package ak.detaysoft.graff;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.*;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

import ak.detaysoft.graff.database_models.L_Application;
import ak.detaysoft.graff.database_models.L_ApplicationCategory;
import ak.detaysoft.graff.database_models.L_Category;
import ak.detaysoft.graff.database_models.L_Content;
import ak.detaysoft.graff.database_models.L_ContentCategory;
import ak.detaysoft.graff.database_models.L_CustomerApplication;
import ak.detaysoft.graff.database_models.L_Statistic;

/**
 * Created by adem on 25/02/14.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = "Galepress.db";
    private static final int DATABASE_VERSION = 15;

    // the DAO object we use to access the SimpleData table
    private Dao<L_CustomerApplication, Integer> customerApplicationDao = null;
    private RuntimeExceptionDao<L_Category, Integer> customerApplicationRuntimeDao = null;

    private Dao<L_Category, Integer> categoriesDao = null;
    private RuntimeExceptionDao<L_Category, Integer> categoriesRuntimeDao = null;


    private Dao<L_ApplicationCategory, Integer> applicationCategoryDao = null;
    private RuntimeExceptionDao<L_Category, Integer> applicationCategoryRuntimeDao = null;

    private Dao<L_Content, Integer> contentsDao = null;
    private RuntimeExceptionDao<L_Content, Integer> contentsRuntimeDao = null;

    private Dao<L_Application, Integer> applicationDao = null;
    private RuntimeExceptionDao<L_Application, Integer> applicationRuntimeDao = null;

    private Dao<L_Statistic, Integer> statisticsDao = null;
    private RuntimeExceptionDao<L_Statistic, Integer> statisticsRuntimeDao = null;

    private Dao<L_ContentCategory, Integer> contentCategoryDao = null;
    private RuntimeExceptionDao<L_ContentCategory, Integer> contentCategoryRuntimeDao = null;


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is first created. Usually you should call createTable statements here to create
     * the tables that will store your data.
     */
    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, L_CustomerApplication.class);
            TableUtils.createTable(connectionSource, L_Category.class);
            TableUtils.createTable(connectionSource, L_ApplicationCategory.class);
            TableUtils.createTable(connectionSource, L_Content.class);
            TableUtils.createTable(connectionSource, L_Application.class);
            TableUtils.createTable(connectionSource, L_Statistic.class);
            TableUtils.createTable(connectionSource, L_ContentCategory.class);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
            throw new RuntimeException(e);
        }


        Integer applicationId = GalePressApplication.getInstance().getApplicationId();
        RuntimeExceptionDao<L_Application, Integer> dao = getApplicationDataDao();
        if(dao.queryForId(applicationId) == null){
            L_Application application = new L_Application(GalePressApplication.getInstance().getApplicationId(), -1);
            dao.create(application);
            Log.i("Galepress", "New Application recoded to db with -1 as version");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        // TODO: This part must be reconfigured for new versions.
        try {
            Log.i(DatabaseHelper.class.getName(), "onUpgrade");
            TableUtils.dropTable(connectionSource, L_CustomerApplication.class, true);
            TableUtils.dropTable(connectionSource, L_Category.class, true);
            TableUtils.dropTable(connectionSource, L_ApplicationCategory.class, true);
            TableUtils.dropTable(connectionSource, L_Content.class, true);
            TableUtils.dropTable(connectionSource, L_Application.class, true);
            TableUtils.dropTable(connectionSource, L_Statistic.class, true);
            TableUtils.dropTable(connectionSource, L_ContentCategory.class, true);
            // after we drop the old databases, we create the new ones
            onCreate(db, connectionSource);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can't drop databases", e);
            throw new RuntimeException(e);
        }
    }
    /**
     * Returns the Database Access Object (DAO) for our SimpleData class. It will create it or just give the cached
     * value.
     */

    public Dao<L_CustomerApplication, Integer> getcustomerApplicationDao() throws SQLException {
        if (customerApplicationDao == null) {
            customerApplicationDao = getDao(L_CustomerApplication.class);
        }
        return customerApplicationDao;
    }


    public Dao<L_Category, Integer> getCategoriesDao() throws SQLException {
        if (categoriesDao == null) {
            categoriesDao = getDao(L_Category.class);
        }
        return categoriesDao;
    }


    public Dao<L_ApplicationCategory, Integer> getApplicationCategoryDao() throws SQLException {
        if (applicationCategoryDao == null) {
            applicationCategoryDao = getDao(L_ApplicationCategory.class);
        }
        return applicationCategoryDao;
    }
    /**
     * Returns the RuntimeExceptionDao (Database Access Object) version of a Dao for our SimpleData class. It will
     * create it or just give the cached value. RuntimeExceptionDao only through RuntimeExceptions.
     */
    public RuntimeExceptionDao<L_Category, Integer> getCategoriesDataDao() {
        if (categoriesRuntimeDao == null) {
            categoriesRuntimeDao= getRuntimeExceptionDao(L_Category.class);
        }
        return categoriesRuntimeDao;
    }



    public Dao<L_Content, Integer> getContentsDao() throws SQLException {
        if (contentsDao == null) {
            contentsDao = getDao(L_Content.class);
        }
        return contentsDao;
    }
    public RuntimeExceptionDao<L_Content, Integer> getContentsDataDao() {
        if (contentsRuntimeDao== null) {
            contentsRuntimeDao= getRuntimeExceptionDao(L_Content.class);
        }
        return contentsRuntimeDao;
    }



    public Dao<L_Application, Integer> getApplicationDao() throws SQLException {
        if (applicationDao == null) {
            applicationDao = getDao(L_Application.class);
        }
        return applicationDao;
    }
    public RuntimeExceptionDao<L_Application, Integer> getApplicationDataDao() {
        if (applicationRuntimeDao== null) {
            applicationRuntimeDao= getRuntimeExceptionDao(L_Application.class);
        }
        return applicationRuntimeDao;
    }



    public Dao<L_Statistic, Integer> getStatisticsDao() throws SQLException {
        if (statisticsDao == null) {
            statisticsDao = getDao(L_Statistic.class);
        }
        return statisticsDao;
    }
    public RuntimeExceptionDao<L_Statistic, Integer> getStatisticsDataDao() {
        if (statisticsRuntimeDao== null) {
            statisticsRuntimeDao= getRuntimeExceptionDao(L_Statistic.class);
        }
        return statisticsRuntimeDao;
    }


    public Dao<L_ContentCategory, Integer> getContentCategoryDao() throws SQLException {
        if (contentCategoryDao == null) {
            contentCategoryDao = getDao(L_ContentCategory.class);
        }
        return contentCategoryDao;
    }
    public RuntimeExceptionDao<L_ContentCategory, Integer> getContentCategoryDataDao() {
        if (contentCategoryRuntimeDao== null) {
            contentCategoryRuntimeDao= getRuntimeExceptionDao(L_ContentCategory.class);
        }
        return contentCategoryRuntimeDao;
    }


    /**
     * Close the database connections and clear any cached DAOs.
     */
    @Override
    public void close() {
        super.close();
        contentsDao = null;
        customerApplicationDao = null;
        categoriesDao = null;
        applicationCategoryDao = null;
        contentCategoryDao = null;
        applicationDao = null;
        statisticsDao = null;
    }
}