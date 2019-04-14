/*
 * This file is generated by jOOQ.
 */
package com.rainday.gen.tables;


import com.rainday.gen.Indexes;
import com.rainday.gen.Keys;
import com.rainday.gen.Public;
import com.rainday.gen.tables.records.AppRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;

import javax.annotation.Generated;
import java.util.Arrays;
import java.util.List;


/**
 * app信息表
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.11"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class App extends TableImpl<AppRecord> {

    private static final long serialVersionUID = -1387473576;

    /**
     * The reference instance of <code>PUBLIC.APP</code>
     */
    public static final App APP = new App();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<AppRecord> getRecordType() {
        return AppRecord.class;
    }

    /**
     * The column <code>PUBLIC.APP.ID</code>. 指针主键
     */
    public final TableField<AppRecord, Integer> ID = createField("ID", org.jooq.impl.SQLDataType.INTEGER.nullable(false).identity(true), this, "指针主键");

    /**
     * The column <code>PUBLIC.APP.APPKEY</code>. app唯一ID
     */
    public final TableField<AppRecord, String> APPKEY = createField("APPKEY", org.jooq.impl.SQLDataType.VARCHAR(10).nullable(false), this, "app唯一ID");

    /**
     * The column <code>PUBLIC.APP.APPNAME</code>. app名称
     */
    public final TableField<AppRecord, String> APPNAME = createField("APPNAME", org.jooq.impl.SQLDataType.VARCHAR(20), this, "app名称");

    /**
     * The column <code>PUBLIC.APP.PORT</code>. 端口
     */
    public final TableField<AppRecord, Integer> PORT = createField("PORT", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "端口");

    /**
     * The column <code>PUBLIC.APP.DESCRIPTION</code>. app简介
     */
    public final TableField<AppRecord, String> DESCRIPTION = createField("DESCRIPTION", org.jooq.impl.SQLDataType.VARCHAR(100), this, "app简介");

    /**
     * The column <code>PUBLIC.APP.STATUS</code>. app状态
     */
    public final TableField<AppRecord, String> STATUS = createField("STATUS", org.jooq.impl.SQLDataType.VARCHAR(10).defaultValue(DSL.field("'inactive'", org.jooq.impl.SQLDataType.VARCHAR)), this, "app状态");

    /**
     * The column <code>PUBLIC.APP.DEPLOYID</code>. deployId
     */
    public final TableField<AppRecord, String> DEPLOYID = createField("DEPLOYID", org.jooq.impl.SQLDataType.VARCHAR(36), this, "deployId");

    /**
     * Create a <code>PUBLIC.APP</code> table reference
     */
    public App() {
        this(DSL.name("APP"), null);
    }

    /**
     * Create an aliased <code>PUBLIC.APP</code> table reference
     */
    public App(String alias) {
        this(DSL.name(alias), APP);
    }

    /**
     * Create an aliased <code>PUBLIC.APP</code> table reference
     */
    public App(Name alias) {
        this(alias, APP);
    }

    private App(Name alias, Table<AppRecord> aliased) {
        this(alias, aliased, null);
    }

    private App(Name alias, Table<AppRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment("app信息表"));
    }

    public <O extends Record> App(Table<O> child, ForeignKey<O, AppRecord> key) {
        super(child, key, APP);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.IDX_APPKEY, Indexes.IDX_DEPLOYID, Indexes.PRIMARY_KEY_F);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity<AppRecord, Integer> getIdentity() {
        return Keys.IDENTITY_APP;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<AppRecord> getPrimaryKey() {
        return Keys.APP_PK;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<AppRecord>> getKeys() {
        return Arrays.<UniqueKey<AppRecord>>asList(Keys.APP_PK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public App as(String alias) {
        return new App(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public App as(Name alias) {
        return new App(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public App rename(String name) {
        return new App(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public App rename(Name name) {
        return new App(name, null);
    }
}
