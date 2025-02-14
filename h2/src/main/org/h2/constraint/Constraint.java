/*
 * Copyright 2004-2023 H2 Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (https://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.constraint;

import java.util.HashSet;
import org.h2.engine.DbObject;
import org.h2.engine.SessionLocal;
import org.h2.expression.Expression;
import org.h2.expression.ExpressionVisitor;
import org.h2.index.Index;
import org.h2.message.Trace;
import org.h2.result.Row;
import org.h2.schema.Schema;
import org.h2.schema.SchemaObject;
import org.h2.table.Column;
import org.h2.table.Table;

/**
 * The base class for constraint checking.
 */
public abstract class Constraint extends SchemaObject implements Comparable<Constraint> {

    public enum Type {
        /**
         * The constraint type for check constraints.
         */
        CHECK,
        /**
         * The constraint type for primary key constraints.
         */
        PRIMARY_KEY,
        /**
         * The constraint type for unique constraints.
         */
        UNIQUE,
        /**
         * The constraint type for referential constraints.
         */
        REFERENTIAL,
        /**
         * The constraint type for domain constraints.
         */
        DOMAIN;

        /**
         * Get standard SQL type name.
         *
         * @return standard SQL type name
         */
        public String getSqlName() {
            if (this == PRIMARY_KEY) {
                return "PRIMARY KEY";
            }
            if (this == REFERENTIAL) {
                return "FOREIGN KEY";
            }
            return name();
        }

        /**
         * Tests whether this type is a check or domain type or not.
         *
         * @return {@code true} if this type is a check or a domain type,
         *         {@code false} otherwise
         */
        public boolean isCheck() {
            return this == CHECK || this == DOMAIN;
        }

        /**
         * Tests whether this type is a primary key or unique or not.
         *
         * @return {@code true} if this type is a primary key or unique type,
         *         {@code false} otherwise
         */
        public boolean isUnique() {
            return this == PRIMARY_KEY || this == UNIQUE;
        }

    }

    /**
     * The table for which this constraint is defined.
     */
    protected Table table;

    Constraint(Schema schema, int id, String name, Table table) {
        super(schema, id, name, Trace.CONSTRAINT);
        this.table = table;
        if (table != null) {
            this.setTemporary(table.isTemporary());
        }
    }

    /**
     * The constraint type name
     *
     * @return the name
     */
    public abstract Type getConstraintType();

    /**
     * Check if this row fulfils the constraint.
     * This method throws an exception if not.
     *
     * @param session the session
     * @param t the table
     * @param oldRow the old row
     * @param newRow the new row
     */
    public abstract void checkRow(SessionLocal session, Table t, Row oldRow, Row newRow);

    /**
     * Check if this constraint needs the specified index.
     *
     * @param index the index
     * @return true if the index is used
     */
    public abstract boolean usesIndex(Index index);

    /**
     * This index is now the owner of the specified index.
     *
     * @param index the index
     */
    public abstract void setIndexOwner(Index index);

    /**
     * Get all referenced columns.
     *
     * @param table the table
     * @return the set of referenced columns
     */
    public abstract HashSet<Column> getReferencedColumns(Table table);

    /**
     * Returns the CHECK expression or null.
     *
     * @return the CHECK expression or null.
     */
    public Expression getExpression() {
        return null;
    }

    /**
     * Get the SQL statement to create this constraint.
     *
     * @return the SQL statement
     */
    public abstract String  getCreateSQLWithoutIndexes();

    /**
     * Check if this constraint needs to be checked before updating the data.
     *
     * @return true if it must be checked before updating
     */
    public abstract boolean isBefore();

    /**
     * Check the existing data. This method is called if the constraint is added
     * after data has been inserted into the table.
     *
     * @param session the session
     */
    public abstract void checkExistingData(SessionLocal session);

    /**
     * This method is called after a related table has changed
     * (the table was renamed, or columns have been renamed).
     */
    public abstract void rebuild();

    /**
     * Get the index of this constraint in the source table, or null if no index
     * is used.
     *
     * @return the index
     */
    public Index getIndex() {
        return null;
    }

    /**
     * Returns the referenced unique constraint, or null.
     *
     * @return the referenced unique constraint, or null
     */
    public ConstraintUnique getReferencedConstraint() {
        return null;
    }

    @Override
    public int getType() {
        return DbObject.CONSTRAINT;
    }

    public Table getTable() {
        return table;
    }

    public Table getRefTable() {
        return table;
    }

    @Override
    public int compareTo(Constraint other) {
        if (this == other) {
            return 0;
        }
        return Integer.compare(getConstraintType().ordinal(), other.getConstraintType().ordinal());
    }

    /**
     * Visit all elements in the constraint.
     *
     * @param visitor the visitor
     * @return true if every visited expression returned true, or if there are
     *         no expressions
     */
    public boolean isEverything(@SuppressWarnings("unused") ExpressionVisitor visitor) {
        return true;
    }

}
