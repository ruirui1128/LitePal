/*
 * Copyright (C)  Tony Green, LitePal Framework Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.litepal.tablemanager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import org.litepal.LitePalApplication;
import org.litepal.Operator;
import org.litepal.parser.LitePalAttr;
import org.litepal.tablemanager.callback.DatabaseListener;
import org.litepal.util.SharedUtil;

/**
 * The database helper to generate and manage the tables. It will automate
 * create or upgrade the database file depends on the parameters passed in.
 * <p>
 * LitePal makes it easy for managing tables. It used the dynamic features of
 * Java with reflection API to achieve that. Developers won't need to write
 * their own SQL for managing tables, LitePal will do that for them. Developers
 * just need to write their model classes and add right associations. LitePal
 * will take all the rest job to manager tables in database.
 *
 * @author Tony Green
 * @since 1.0
 */
class LitePalOpenHelper extends SQLiteOpenHelper {
    public static final String TAG = "LitePalHelper";

    /**
     * The standard constructor for SQLiteOpenHelper.
     *
     * @param context To use to open or create the database.
     * @param name    The database file.
     * @param factory To use for creating cursor objects, or null for the default
     *                version number of the database (starting at 1); if the
     *                database is older, onUpgrade.
     * @param version (SQLiteDatabase, int, int) will be used to upgrade the
     *                database; if the database is newer,
     *                onDowngrade(SQLiteDatabase, int, int) will be used to
     *                downgrade the database
     */
    LitePalOpenHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    /**
     * A simple constructor for SQLiteOpenHelper with null for CursorFactory as
     * default.
     *
     * @param dbName  The database file.
     * @param version (SQLiteDatabase, int, int) will be used to upgrade the
     *                database; if the database is newer,
     *                onDowngrade(SQLiteDatabase, int, int) will be used to
     *                downgrade the database
     */
    LitePalOpenHelper(String dbName, int version) {
        this(LitePalApplication.getContext(), dbName, null, version);
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
//            SQLiteDatabase.OpenParams.Builder builder = new SQLiteDatabase.OpenParams.Builder();
//            builder.setSynchronousMode("FULL");
//            setOpenParams(builder.build());
//        }

    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        //db.enableWriteAheadLogging();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Generator.create(db);

        final DatabaseListener listener = Operator.getDBListener();
        if (listener != null) {
            LitePalApplication.sHandler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onCreate();
                }
            });
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, final int oldVersion, final int newVersion) {
        Generator.upgrade(db);
        SharedUtil.updateVersion(LitePalAttr.getInstance().getExtraKeyName(), newVersion);
        final DatabaseListener listener = Operator.getDBListener();
        if (listener != null) {
            LitePalApplication.sHandler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onUpgrade(oldVersion, newVersion);
                }
            });
        }
    }

}