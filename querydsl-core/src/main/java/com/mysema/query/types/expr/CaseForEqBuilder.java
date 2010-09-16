/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.query.types.expr;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.mysema.query.types.Expression;
import com.mysema.query.types.Operator;
import com.mysema.query.types.Ops;

/**
 * CaseForEqBuilder enables the construction of typesafe case-when-then-else constructs
 * for equals-operations :
 * e.g.
 *
 * <pre>
 * QCustomer c = QCustomer.customer;
 * Expr<Integer> cases = c.annualSpending
 *     .when(1000l).then(1)
 *     .when(2000l).then(2)
 *     .when(5000l).then(3)
 *     .otherwise(4);
 * </pre>
 *
 * @author tiwe
 *
 * @param <D>
 */
public final class CaseForEqBuilder<D> {

    private static class CaseElement<D> {

        @Nullable
        private final Expression<? extends D> eq;

        private final Expression<?> target;

        public CaseElement(@Nullable Expression<? extends D> eq, Expression<?> target){
            this.eq = eq;
            this.target = target;
        }

        public Expression<? extends D> getEq() {
            return eq;
        }

        public Expression<?> getTarget() {
            return target;
        }

    }

    private final Expression<D> base;

    private final Expression<? extends D> other;

    private final List<CaseElement<D>> caseElements = new ArrayList<CaseElement<D>>();

    private Class<?> type;

    public CaseForEqBuilder(Expression<D> base, Expression<? extends D> other) {
        this.base = base;
        this.other = other;
    }

    public <T> Cases<T,Expression<T>> then(Expression<T> then){
        type = then.getType();
        return new Cases<T,Expression<T>>(){
            @Override
            protected Expression<T> createResult(Class<T> type, Expression<T> last) {
                return SimpleOperation.create((Class<T>)type, Ops.CASE_EQ, base, last);
            }
        }.when(other).then(then);
    }

    public <T> Cases<T,Expression<T>> then(T then){
        return then(SimpleConstant.create(then));
    }

    public <T extends Number & Comparable<?>> Cases<T,NumberExpression<T>> then(T then){
        return then(NumberConstant.create(then));
    }

    public <T extends Number & Comparable<?>> Cases<T,NumberExpression<T>> then(NumberExpression<T> then){
        type = then.getType();
        return new Cases<T,NumberExpression<T>>(){
            @SuppressWarnings("unchecked")
            @Override
            protected NumberExpression<T> createResult(Class<T> type, Expression<T> last) {
                return NumberOperation.create(type, (Operator)Ops.CASE_EQ, base, last);
            }

        }.when(other).then(then);
    }

    public Cases<String,StringExpression> then(StringExpression then){
        type = then.getType();
        return new Cases<String,StringExpression>(){
            @SuppressWarnings("unchecked")
            @Override
            protected StringExpression createResult(Class<String> type, Expression<String> last) {
                return StringOperation.create((Operator)Ops.CASE_EQ, base, last);
            }

        }.when(other).then(then);
    }

    public Cases<String,StringExpression> then(String then){
        return then(StringConstant.create(then));
    }

    public abstract class Cases<T, Q extends Expression<T>> {

        public CaseWhen<T,Q> when(Expression<? extends D> when){
            return new CaseWhen<T,Q>(this, when);
        }

        public CaseWhen<T,Q> when(D when){
            return when(SimpleConstant.create(when));
        }

        @SuppressWarnings("unchecked")
        public Q otherwise(Expression<T> otherwise){
            caseElements.add(0, new CaseElement<D>(null, otherwise));
            Expression<T> last = null;
            for (CaseElement<D> element : caseElements){
                if (last == null){
                    last = SimpleOperation.create((Class<T>)type, Ops.CASE_EQ_ELSE,
                            element.getTarget());
                }else{
                    last = SimpleOperation.create((Class<T>)type, Ops.CASE_EQ_WHEN,
                            base,
                            element.getEq(),
                            element.getTarget(),
                            last);
                }
            }
            return createResult((Class<T>)type, last);
        }

        protected abstract Q createResult(Class<T> type, Expression<T> last);

        public Q otherwise(T otherwise){
            return otherwise(SimpleConstant.create(otherwise));
        }
    }

    public class CaseWhen<T, Q extends Expression<T>> {

        private final Cases<T, Q> cases;

        private final Expression<? extends D> when;

        public CaseWhen(Cases<T, Q> cases, Expression<? extends D> when) {
            this.cases = cases;
            this.when = when;
        }

        public Cases<T, Q> then(Expression<T> then){
            caseElements.add(0, new CaseElement<D>(when, then));
            return cases;
        }

        public Cases<T, Q> then(T then){
            return then(SimpleConstant.create(then));
        }

    }

}
