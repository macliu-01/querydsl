/*
 * Copyright (c) 2008 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.query.grammar;

import java.lang.reflect.Method;

import com.mysema.core.collection.FactoryMap;
import com.mysema.query.grammar.Types.*;

/** 
 * Visitor provides a dispatching Visitor for Expr instances
 *
 * @author tiwe
 * @version $Id$
 */
public abstract class Visitor<T extends Visitor<T>> {
    
    private final FactoryMap<Class<?>,Method> methodMap = new FactoryMap<Class<?>,Method>(){

        @Override
        protected Method create(Class<?> cl) {
            while (!Types.class.equals(cl.getEnclosingClass())){
                cl = cl.getSuperclass();
            }
            try {
                Method method = null;
                Class<?> sigClass = Visitor.this.getClass();
                while (method == null && !sigClass.equals(Visitor.class)){
                    try{
                        method = sigClass.getDeclaredMethod("visit", cl);    
                    }catch(NoSuchMethodException nsme){
                        sigClass = sigClass.getSuperclass();
                    }                       
                }
                if (method != null){
                    method.setAccessible(true);    
                }                
                return method;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        
    };
    
    @SuppressWarnings("unchecked")
    public final T handle(Expr<?> expr){
        try {
            methodMap.get(expr.getClass()).invoke(this, expr);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return (T) this;
    }
    
    protected abstract void visit(Alias<?> expr);
    
    protected abstract void visit(AliasForAnything<?> expr);
    
    protected abstract void visit(AliasForCollection<?> expr);
    
    protected abstract void visit(AliasForEntity<?> expr);
    
    protected abstract void visit(OperationBinaryBoolean<?,?> expr);
    
    protected abstract void visit(OperationBinary<?,?,?,?> expr);
    
    protected abstract void visit(PathBoolean expr);
    
    protected abstract void visit(PathCollection<?> expr);
    
    protected abstract void visit(ConstantExpr<?> expr);
    
    protected abstract void visit(CountExpr<?> expr);
    
    protected abstract void visit(PathDomainType<?> expr);
    
    protected abstract void visit(Path<?> expr);
    
    protected abstract void visit(OperationTertiaryBoolean<?,?,?> expr);
    
    protected abstract void visit(OperationTertiary<?,?,?,?,?> expr);
    
    protected abstract void visit(OperationUnaryBoolean<?> expr);
    
    protected abstract void visit(OperationUnary<?,?,?> expr);
}
