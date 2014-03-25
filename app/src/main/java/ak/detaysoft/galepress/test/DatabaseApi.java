package ak.detaysoft.galepress.test;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;

import java.sql.SQLException;
import java.util.List;

import ak.detaysoft.galepress.GalePressApplication;
import ak.detaysoft.galepress.database_models.L_Application;
import ak.detaysoft.galepress.database_models.L_Category;
import ak.detaysoft.galepress.database_models.L_Content;
import ak.detaysoft.galepress.database_models.L_ContentCategory;
import ak.detaysoft.galepress.database_models.L_Statistic;

/**
 * Created by adem on 25/02/14.
 */
public class DatabaseApi {

    private DatabaseHelper db;
    private Dao<L_Category, Integer> categoriesDao;
    private Dao<L_Content, Integer> contentsDao;
    private Dao<L_ContentCategory, Integer> contentCategoryDao;
    private Dao<L_Application, Integer> applicationsDao;
    private Dao<L_Statistic, Integer> statisticsDao;
    private PreparedQuery<L_Content> contentsByCategoryQuery = null;
    private PreparedQuery<L_ContentCategory> contentCategoryByContentQuery = null;


    public DatabaseApi(Context ctx)
    {
        try {
            DatabaseManager dbManager = new DatabaseManager();
            db = dbManager.getHelper(ctx);
            categoriesDao = db.getCategoriesDao();
            contentsDao = db.getContentsDao();
            contentCategoryDao = db.getContentCategoryDao();
            applicationsDao = db.getApplicationDao();
            statisticsDao = db.getStatisticsDao();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Methods for CATEGORY
     * */
    public int createCategory(L_Category category)
    {
        try {
            return categoriesDao.create(category);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    public int updateCategory(L_Category category)
    {
        try {
            return categoriesDao.update(category);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    public int deleteCategory(L_Category category)
    {
        try {
            return categoriesDao.delete(category);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List getAllCategories()
    {
        try {
            return categoriesDao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public L_Category getCategory(Integer id)
    {
        try {
            return categoriesDao.queryForId(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }



    /**
     * Methods for CONTENT
     */
    public int createContent(L_Content content)
    {
        try {
            return contentsDao.create(content);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    public int updateContent(L_Content content)
    {
        int result = 0 ;
        try {
            Log.i("Adem","Content updated (dba) Content:"+content.toString());
            result = contentsDao.update(content);
        } catch (SQLException e) {
            result = 0;
            e.printStackTrace();
        } finally {
            GalePressApplication.getInstance().getLibraryActivity().updateGridView();
        }
        return result;
    }
    public int deleteContent(L_Content content)
    {
        int result = 0;
        try {
            result = contentsDao.delete(content);
        } catch (SQLException e) {
            result = 0;
            e.printStackTrace();
        } finally {
            GalePressApplication.getInstance().getLibraryActivity().updateGridView();
        }
        return result;
    }

    public L_Content getContent(Integer id)
    {
        try {
            return contentsDao.queryForId(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List getAllContents()
    {
        try {
            return contentsDao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List getAllContentsByCategory(L_Category category)
    {
        try {
            if (contentsByCategoryQuery == null) {
                contentsByCategoryQuery= makeContentsForCategoryQuery();
            }
            contentsByCategoryQuery.setArgumentHolderValue(0, category);
            return contentsDao.query(contentsByCategoryQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
    private PreparedQuery<L_Content> makeContentsForCategoryQuery() throws SQLException {
        QueryBuilder<L_ContentCategory, Integer> categoryContent = contentCategoryDao.queryBuilder();
        categoryContent.selectColumns(L_ContentCategory.CONTENT_ID_FIELD_NAME);
        SelectArg userSelectArg = new SelectArg();
        categoryContent.where().eq(L_ContentCategory.CATEGORY_ID_FIELD_NAME, userSelectArg);
        QueryBuilder<L_Content, Integer> contentObj = contentsDao.queryBuilder();
        contentObj.where().in(L_Content.ID_FIELD_NAME, categoryContent);
        return contentObj.prepare();
    }
    /**
     * Methods for Application
     * */
    public int createApplication(L_Application application)
    {
        try {
            return applicationsDao.create(application);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    public int updateApplication(L_Application application)
    {
        try {
            return applicationsDao.update(application);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    public int deleteApplication(L_Application application)
    {
        try {
            return applicationsDao.delete(application);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    public L_Application getApplication(Integer id)
    {
        try {
            return applicationsDao.queryForId(id);
        } catch (SQLException e) {
            Log.e("W/System.err","Application may not be exist at first start.. We will create new record with -1 version.");
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Statistic Methods
     * */
    public int createStatistic(L_Statistic statistic)
    {
        try {
            return statisticsDao.create(statistic);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    public int updateStatistic(L_Statistic statistic)
    {
        try {
            return statisticsDao.update(statistic);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    public int deleteStatistic(L_Statistic statistic)
    {
        try {
            return statisticsDao.delete(statistic);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List getAllStatistics()
    {
        try {
            return statisticsDao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Methods for CONTENT_CATEGORY
     * */
    public int createContentCategory(L_ContentCategory contentCategory)
    {
        try {
            return contentCategoryDao.create(contentCategory);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    public int updateCategory(L_ContentCategory contentCategory)
    {
        try {
            return contentCategoryDao.update(contentCategory);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    public int deleteContentCategory(L_ContentCategory contentCategory)
    {
        try {
            return contentCategoryDao.delete(contentCategory);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List getAllContentCategories()
    {
        try {
            return contentCategoryDao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public List getAllContentCategoryByContent(L_Content content)
    {
        try {
            if (contentCategoryByContentQuery == null) {
                contentCategoryByContentQuery= makeContentCategoryForContentQuery();
            }
            contentCategoryByContentQuery.setArgumentHolderValue(0, content);
            return contentCategoryDao.query(contentCategoryByContentQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    private PreparedQuery<L_ContentCategory> makeContentCategoryForContentQuery() throws SQLException {
        QueryBuilder<L_ContentCategory, Integer> categoryContent = contentCategoryDao.queryBuilder();
        categoryContent.selectColumns(L_ContentCategory.CONTENT_ID_FIELD_NAME);
        SelectArg userSelectArg = new SelectArg();
        categoryContent.where().eq(L_ContentCategory.CONTENT_ID_FIELD_NAME, userSelectArg);
        return categoryContent.prepare();
    }
}