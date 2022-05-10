package sdk.chat.core.dao;

import java.util.List;
import java.util.ArrayList;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.SqlUtils;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;
import org.greenrobot.greendao.query.Query;
import org.greenrobot.greendao.query.QueryBuilder;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "USER_THREAD_LINK_META_VALUE".
*/
public class UserThreadLinkMetaValueDao extends AbstractDao<UserThreadLinkMetaValue, Long> {

    public static final String TABLENAME = "USER_THREAD_LINK_META_VALUE";

    /**
     * Properties of entity UserThreadLinkMetaValue.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Key = new Property(1, String.class, "key", false, "KEY");
        public final static Property Value = new Property(2, String.class, "value", false, "VALUE");
        public final static Property UserThreadLinkId = new Property(3, Long.class, "userThreadLinkId", false, "USER_THREAD_LINK_ID");
    }

    private DaoSession daoSession;

    private Query<UserThreadLinkMetaValue> userThreadLink_MetaValuesQuery;

    public UserThreadLinkMetaValueDao(DaoConfig config) {
        super(config);
    }
    
    public UserThreadLinkMetaValueDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"USER_THREAD_LINK_META_VALUE\" (" + //
                "\"_id\" INTEGER PRIMARY KEY ," + // 0: id
                "\"KEY\" TEXT," + // 1: key
                "\"VALUE\" TEXT," + // 2: value
                "\"USER_THREAD_LINK_ID\" INTEGER);"); // 3: userThreadLinkId
        // Add Indexes
        db.execSQL("CREATE INDEX " + constraint + "IDX_USER_THREAD_LINK_META_VALUE_KEY ON \"USER_THREAD_LINK_META_VALUE\"" +
                " (\"KEY\" ASC);");
        db.execSQL("CREATE INDEX " + constraint + "IDX_USER_THREAD_LINK_META_VALUE_VALUE ON \"USER_THREAD_LINK_META_VALUE\"" +
                " (\"VALUE\" ASC);");
        db.execSQL("CREATE INDEX " + constraint + "IDX_USER_THREAD_LINK_META_VALUE_USER_THREAD_LINK_ID ON \"USER_THREAD_LINK_META_VALUE\"" +
                " (\"USER_THREAD_LINK_ID\" ASC);");
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"USER_THREAD_LINK_META_VALUE\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, UserThreadLinkMetaValue entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String key = entity.getKey();
        if (key != null) {
            stmt.bindString(2, key);
        }
 
        String value = entity.getValue();
        if (value != null) {
            stmt.bindString(3, value);
        }
 
        Long userThreadLinkId = entity.getUserThreadLinkId();
        if (userThreadLinkId != null) {
            stmt.bindLong(4, userThreadLinkId);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, UserThreadLinkMetaValue entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String key = entity.getKey();
        if (key != null) {
            stmt.bindString(2, key);
        }
 
        String value = entity.getValue();
        if (value != null) {
            stmt.bindString(3, value);
        }
 
        Long userThreadLinkId = entity.getUserThreadLinkId();
        if (userThreadLinkId != null) {
            stmt.bindLong(4, userThreadLinkId);
        }
    }

    @Override
    protected final void attachEntity(UserThreadLinkMetaValue entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public UserThreadLinkMetaValue readEntity(Cursor cursor, int offset) {
        UserThreadLinkMetaValue entity = new UserThreadLinkMetaValue( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // key
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // value
            cursor.isNull(offset + 3) ? null : cursor.getLong(offset + 3) // userThreadLinkId
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, UserThreadLinkMetaValue entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setKey(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setValue(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setUserThreadLinkId(cursor.isNull(offset + 3) ? null : cursor.getLong(offset + 3));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(UserThreadLinkMetaValue entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(UserThreadLinkMetaValue entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(UserThreadLinkMetaValue entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
    /** Internal query to resolve the "metaValues" to-many relationship of UserThreadLink. */
    public List<UserThreadLinkMetaValue> _queryUserThreadLink_MetaValues(Long userThreadLinkId) {
        synchronized (this) {
            if (userThreadLink_MetaValuesQuery == null) {
                QueryBuilder<UserThreadLinkMetaValue> queryBuilder = queryBuilder();
                queryBuilder.where(Properties.UserThreadLinkId.eq(null));
                userThreadLink_MetaValuesQuery = queryBuilder.build();
            }
        }
        Query<UserThreadLinkMetaValue> query = userThreadLink_MetaValuesQuery.forCurrentThread();
        query.setParameter(0, userThreadLinkId);
        return query.list();
    }

    private String selectDeep;

    protected String getSelectDeep() {
        if (selectDeep == null) {
            StringBuilder builder = new StringBuilder("SELECT ");
            SqlUtils.appendColumns(builder, "T", getAllColumns());
            builder.append(',');
            SqlUtils.appendColumns(builder, "T0", daoSession.getUserThreadLinkDao().getAllColumns());
            builder.append(" FROM USER_THREAD_LINK_META_VALUE T");
            builder.append(" LEFT JOIN USER_THREAD_LINK T0 ON T.\"USER_THREAD_LINK_ID\"=T0.\"_id\"");
            builder.append(' ');
            selectDeep = builder.toString();
        }
        return selectDeep;
    }
    
    protected UserThreadLinkMetaValue loadCurrentDeep(Cursor cursor, boolean lock) {
        UserThreadLinkMetaValue entity = loadCurrent(cursor, 0, lock);
        int offset = getAllColumns().length;

        UserThreadLink userThreadLink = loadCurrentOther(daoSession.getUserThreadLinkDao(), cursor, offset);
        entity.setUserThreadLink(userThreadLink);

        return entity;    
    }

    public UserThreadLinkMetaValue loadDeep(Long key) {
        assertSinglePk();
        if (key == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder(getSelectDeep());
        builder.append("WHERE ");
        SqlUtils.appendColumnsEqValue(builder, "T", getPkColumns());
        String sql = builder.toString();
        
        String[] keyArray = new String[] { key.toString() };
        Cursor cursor = db.rawQuery(sql, keyArray);
        
        try {
            boolean available = cursor.moveToFirst();
            if (!available) {
                return null;
            } else if (!cursor.isLast()) {
                throw new IllegalStateException("Expected unique result, but count was " + cursor.getCount());
            }
            return loadCurrentDeep(cursor, true);
        } finally {
            cursor.close();
        }
    }
    
    /** Reads all available rows from the given cursor and returns a list of new ImageTO objects. */
    public List<UserThreadLinkMetaValue> loadAllDeepFromCursor(Cursor cursor) {
        int count = cursor.getCount();
        List<UserThreadLinkMetaValue> list = new ArrayList<UserThreadLinkMetaValue>(count);
        
        if (cursor.moveToFirst()) {
            if (identityScope != null) {
                identityScope.lock();
                identityScope.reserveRoom(count);
            }
            try {
                do {
                    list.add(loadCurrentDeep(cursor, false));
                } while (cursor.moveToNext());
            } finally {
                if (identityScope != null) {
                    identityScope.unlock();
                }
            }
        }
        return list;
    }
    
    protected List<UserThreadLinkMetaValue> loadDeepAllAndCloseCursor(Cursor cursor) {
        try {
            return loadAllDeepFromCursor(cursor);
        } finally {
            cursor.close();
        }
    }
    

    /** A raw-style query where you can pass any WHERE clause and arguments. */
    public List<UserThreadLinkMetaValue> queryDeep(String where, String... selectionArg) {
        Cursor cursor = db.rawQuery(getSelectDeep() + where, selectionArg);
        return loadDeepAllAndCloseCursor(cursor);
    }
 
}
