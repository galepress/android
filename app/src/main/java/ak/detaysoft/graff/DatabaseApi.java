package ak.detaysoft.graff;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;

import java.sql.SQLException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
public class DatabaseApi {

    private DatabaseHelper db;
    private Dao<L_Category, Integer> categoriesDao;
    private Dao<L_Content, Integer> contentsDao;
    private Dao<L_ContentCategory, Integer> contentCategoryDao;
    private Dao<L_Application, Integer> applicationsDao;
    private Dao<L_Statistic, Integer> statisticsDao;
    private Dao<L_CustomerApplication, Integer> customerApplicationDao;
    private Dao<L_ApplicationCategory, Integer> applicationCategoryDao;
    private PreparedQuery<L_Content> contentsByCategoryQuery = null;
    private PreparedQuery<L_Content> contentsQuery = null;
    private PreparedQuery<L_ContentCategory> contentCategoryByContentQuery = null;


    public DatabaseApi(Context ctx)
    {
        try {
            DatabaseManager dbManager = new DatabaseManager();
            db = dbManager.getHelper(ctx);
            customerApplicationDao = db.getcustomerApplicationDao();
            applicationCategoryDao = db.getApplicationCategoryDao();
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
     * Methods for CUSTOMERAPPLICATION
     * */
    public int createCustomerApplication(L_CustomerApplication customerApplication)
    {
        try {
            return customerApplicationDao.create(customerApplication);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int updateCustomerApplication(L_CustomerApplication customerApplication)
    {
        try {
            return customerApplicationDao.update(customerApplication);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int deleteCustomerApplication(L_CustomerApplication customerApplication)
    {
        try {
            return customerApplicationDao.delete(customerApplication);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List getAllCustomerApplications()
    {
        try {
            List customerList = customerApplicationDao.queryForAll();
            return customerList;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public List getAllCustomerApplicationsByCategory(int selectedCategoryId, boolean isDownloaded)
    {
        if(isDownloaded) {
            List contentList = getdownloadedContents(isDownloaded);
            Set<Integer> mapList = new HashSet<Integer>();
            for(int content_counter = 0; content_counter < contentList.size(); content_counter++){
                L_Content content = (L_Content) contentList.get(content_counter);
                mapList.add(Integer.valueOf(content.getApplicationId()));
            }

            List customerApplicationList = new ArrayList();
            for(Integer mapItem : mapList){
                L_CustomerApplication application = getCustomerApplication(mapItem, selectedCategoryId);
                customerApplicationList.add(application);
            }
            return customerApplicationList;

        } else {
            try {
                List customerList = customerApplicationDao.queryBuilder().where().eq("categoryid", ""+selectedCategoryId).query();
                return customerList;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public L_CustomerApplication getCustomerApplication(Integer id)
    {
        try {
            return customerApplicationDao.queryForId(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public L_CustomerApplication getCustomerApplication(Integer id, Integer categoryid)
    {
        try {
            return customerApplicationDao.queryBuilder().where().eq("id", ""+id).and().eq("categoryid", ""+categoryid).queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
    * Methods for ApplicationCategory
    * */

    public int createApplicationCategory(L_ApplicationCategory applicationCategory)
    {
        try {
            return applicationCategoryDao.create(applicationCategory);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int updateApplicationCategory(L_ApplicationCategory applicationCategory)
    {
        try {

            UpdateBuilder<L_ApplicationCategory, Integer> updateBuilder = applicationCategoryDao.updateBuilder();
            updateBuilder.where().eq("category", applicationCategory.getCategory()).and().eq("application", applicationCategory.getApplication());
            updateBuilder.updateColumnValue("application",applicationCategory.getApplication());
            updateBuilder.updateColumnValue("category",applicationCategory.getCategory());
            updateBuilder.updateColumnValue("order",applicationCategory.getOrder());
            updateBuilder.updateColumnValue("coverImageUrl",applicationCategory.getCoverImageUrl());
            updateBuilder.updateColumnValue("isUpdated",applicationCategory.isUpdated());
            return updateBuilder.update();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int deleteApplicationCategory(L_ApplicationCategory applicationCategory)
    {
        try {
            DeleteBuilder<L_ApplicationCategory, Integer> deleteBuilder = applicationCategoryDao.deleteBuilder();
            deleteBuilder.where().eq("category", applicationCategory.getCategory()).and().eq("application", applicationCategory.getApplication());
            return deleteBuilder.delete();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public L_ApplicationCategory getApplicationCategory(L_CustomerApplication application, L_Category category)
    {
        try {
            QueryBuilder<L_ApplicationCategory, Integer> applicationCategoryQB = applicationCategoryDao.queryBuilder();
            return applicationCategoryQB.where().eq("application", application).and().eq("category", category).queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<L_ApplicationCategory> getApplicationCategoryByCategory(L_Category category, boolean isDownloaded)
    {

        try {
            if(isDownloaded) {
                List contentList = getdownloadedContents(isDownloaded);
                Set<Integer> mapList = new HashSet<Integer>();
                for(int content_counter = 0; content_counter < contentList.size(); content_counter++){
                    L_Content content = (L_Content) contentList.get(content_counter);
                    mapList.add(Integer.valueOf(content.getApplicationId()));
                }

                List customerApplicationList = new ArrayList();
                for(Integer mapItem : mapList){
                    L_CustomerApplication application = getCustomerApplication(mapItem);
                    L_ApplicationCategory applicationCategory = getApplicationCategoryByApplication(application).get(0);
                    customerApplicationList.add(applicationCategory);
                }
                return customerApplicationList;

            } else {
                List customerList = applicationCategoryDao.queryBuilder().orderBy("order", false).where().eq("category", category).query();
                return customerList;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList();
    }

    public List<L_ApplicationCategory> getApplicationCategoryByCategory(L_Category category) {
        try {
        QueryBuilder<L_ApplicationCategory, Integer> applicationCategoryQB = applicationCategoryDao.queryBuilder();
        return applicationCategoryQB.where().eq("category", category).query();
    } catch (SQLException e) {
        e.printStackTrace();
    }
        return null;

    }



    public List<L_ApplicationCategory> getApplicationCategoryByApplication(L_CustomerApplication application)
    {
        try {
            QueryBuilder<L_ApplicationCategory, Integer> applicationCategoryQB = applicationCategoryDao.queryBuilder();
            return applicationCategoryQB.where().eq("application", application).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
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
            return categoriesDao.queryBuilder().orderBy("order", true).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List getCategoriesOnlyHaveContent(){
        try {
            List all = categoriesDao.queryForAll();
            List result = new ArrayList();
            for(int i = 0; i <all.size(); i++) {
                if(getAllContentsByCategory((L_Category)all.get(i)).size() > 0)
                    result.add(all.get(i));
            }
            return result;
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
    public int updateContent(L_Content content, boolean updateUI)
    {
        int result = 0 ;
        try {
            result = contentsDao.update(content);
        } catch (SQLException e) {
            result = 0;
            e.printStackTrace();
        } finally {
            if(updateUI) {
                if(GalePressApplication.getInstance().getLibraryFragment()!=null){
                    //GalePressApplication.getInstance().getLibraryFragment().updateGridView();
                    GalePressApplication.getInstance().getLibraryFragment().updateAdapterList(content, false);
                }
            }
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
            try{
                if(GalePressApplication.getInstance().getLibraryFragment()!=null){
                    //GalePressApplication.getInstance().getLibraryFragment().updateGridView();
                    GalePressApplication.getInstance().getLibraryFragment().updateAdapterList(content, false);
                }
            }
            catch (Exception e){

            }
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

    public List getAllContentsWithSqlQuery(String searchQuery) {
        List contents;
        if(searchQuery!= null && searchQuery.length() != 0)
            searchQuery = Normalizer.normalize(searchQuery.trim(), Normalizer.Form.NFD)
                    .replaceAll("[^\\p{ASCII}]", "");
        try {
            // Genel kategorisinin olmadigi durumlarda butun contentler listelenir.
            QueryBuilder<L_Content, Integer> contentQuery = contentsDao.queryBuilder();

            if(searchQuery!= null && searchQuery.length() != 0) {
                Where where = contentQuery.where();
                if(searchQuery!= null && searchQuery.length() != 0){
                    where.like("name_asc", "%"+searchQuery+"%");
                }
            }

            contentQuery.orderBy("contentOrderNo", false);

            contents = contentQuery.query();

        } catch (SQLException e) {
            return new ArrayList<L_Content>();
        }
        return contents;
    }

    public List getdownloadedContents(boolean isOnlyDownloaded){
        List contents;
        try {
            QueryBuilder<L_Content, Integer> contentQuery = contentsDao.queryBuilder();

            if(isOnlyDownloaded) {
                Where where = contentQuery.where();
                int andClause = 0;
                if(isOnlyDownloaded){
                    where.eq("isPdfDownloaded", true);
                    andClause++;
                }
                if(andClause>1){
                    where.and(andClause);
                }
            }

            contentQuery.orderBy("contentOrderNo", false);

            contents = contentQuery.query();

        } catch (SQLException e) {
            return new ArrayList<L_Content>();
        }
        return contents;
    }


    public List getAllContentsWithSqlQuery(L_Category category){
        List contents;
        try {
            //KATEGORI NULL ISE GENEL KATEGORISI, GENLE NULL ISE TUM ICERIKLER CEKILIR
            if(category == null){
                L_Category generalCategory = getCategory(MainActivity.GENEL_CATEGORY_ID);
                if(generalCategory != null){
                    // Genel kategorisine ait contentler listelenecek.
                    QueryBuilder<L_Content, Integer> contentQuery = contentsDao.queryBuilder();

                    Where where = contentQuery.where();

                    where.like("categoryIds", "%<" + generalCategory.getId() + ">%");
                    contentQuery.orderBy("contentOrderNo", false);

                    contents = contentQuery.query();
                }
                else {
                    // Genel kategorisinin olmadigi durumlarda butun contentler listelenir.
                    QueryBuilder<L_Content, Integer> contentQuery = contentsDao.queryBuilder();
                    contentQuery.orderBy("contentOrderNo", false);
                    contents = contentQuery.query();
                }
            } else if(category.getId() == -1) {
                QueryBuilder<L_Content, Integer> contentQuery = contentsDao.queryBuilder();

                Where where = contentQuery.where();
                where.eq("isPdfDownloaded", true);

                contentQuery.orderBy("contentOrderNo", false);

                contents = contentQuery.query();
            }else {
                QueryBuilder<L_Content, Integer> contentQuery = contentsDao.queryBuilder();

                Where where = contentQuery.where();
                where.like("categoryIds", "%<" + category.getId() + ">%");

                contentQuery.orderBy("contentOrderNo", false);

                contents = contentQuery.query();
            }

        } catch (SQLException e) {
            return new ArrayList<L_Content>();
        }
        return contents;
    }


    public List getAllContentsForApplicationIdAndcategoryId(final String applicationId, final Integer categoryId, final boolean isOnlyDownloaded){
        List contents;
        try {
            // Genel kategorisine ait contentler listelenecek.
            QueryBuilder<L_Content, Integer> contentQuery = contentsDao.queryBuilder();

            Where where = contentQuery.where();
            if(isOnlyDownloaded){
                where.eq("isPdfDownloaded", true);
                where.and();
            }
            where.eq("applicationId", applicationId);
            where.and();

            contentQuery.orderBy("contentOrderNo", false);

            contents = contentQuery.query();

        } catch (SQLException e) {
            return new ArrayList<L_Content>();
        }

        /*
        * Eger gelen kategori id -1 ise tum icerikler listeleniyor.
        * */
        List newContentList = new ArrayList();
        if(categoryId.intValue() != -1){
            for(int i = 0; i < contents.size(); i++){
                L_Content content = (L_Content)contents.get(i);
                for(L_Category category : content.getCategorList()){
                    if(category != null && category.getId().intValue() == categoryId.intValue()){
                        newContentList.add(content);
                        break;
                    }
                }
            }
            return newContentList;
        } else {
            return contents;
        }


    }

    public List getAllContents(boolean isOnlyDownloaded, String searchQuery, ArrayList<L_Category> categoryList)
    {
        List contents = null;
        List<L_Content> resultContents = new ArrayList<L_Content>();

        if(categoryList == null || categoryList.size() == 0){ //Kategori listesi null yada bossa

            try {
                L_Category generalCategory = getCategory(MainActivity.GENEL_CATEGORY_ID);
                if(generalCategory != null){
                    // Genel kategorisine ait contentler listelenecek.
                    contents = getAllContentsByCategory(generalCategory);
                }
                else {
                    // Genel kategorisinin olmadigi durumlarda butun contentler listelenir.
                    contents = contentsDao.queryForAll();
                }

                for(int i=0; i<contents.size(); i++){
                    L_Content content = (L_Content )contents.get(i);

                    if(isOnlyDownloaded){
                        // Download Status Filter must be applied.
                        if(!content.isPdfDownloaded()){
                            continue;
                        }
                    }

                    if(searchQuery!=null && searchQuery.compareTo("")!=0){
                        // Search Query Filter must be applied.
                        if(!content.getName().toLowerCase().contains(searchQuery.toLowerCase())){
                            continue;
                        }
                    }
                    //resultContents.add(content);
                }
            } catch (Exception e){
                e.printStackTrace();
                return null;
            }

        } else if(isSelectedCategoriesContainAll(categoryList)){ //Tum kategoriler secilmisse
            try{
                // Show all categories.
                contents = contentsDao.queryForAll();
                resultContents.clear();
                for(int i=0; i<contents.size(); i++){
                    L_Content content = (L_Content )contents.get(i);

                    if(isOnlyDownloaded){
                        // Download Status Filter must be applied.
                        if(!content.isPdfDownloaded()){
                            continue;
                        }
                    }

                    if(searchQuery!=null && searchQuery.compareTo("")!=0){
                        // Search Query Filter must be applied.
                        if(!content.getName().toLowerCase().contains(searchQuery.toLowerCase())){
                            continue;
                        }
                    }
                    resultContents.add(content);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        } else { //Kategorilerin hepsi secilmemisse
            for(L_Category item : categoryList) {
                try {
                    contents = getAllContentsByCategory(item);
                    for(int i=0; i<contents.size(); i++){
                        L_Content content = (L_Content )contents.get(i);

                        if(isOnlyDownloaded){
                            // Download Status Filter must be applied.
                            if(!content.isPdfDownloaded()){
                                continue;
                            }
                        }

                        if(searchQuery!=null && searchQuery.compareTo("")!=0){
                            // Search Query Filter must be applied.
                            if(!content.getName().toLowerCase().contains(searchQuery.toLowerCase())){
                                continue;
                            }
                        }
                        resultContents.add(content);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }

        //eger bir content birden fazla kategoride varsa sonuc listesinde birden fazla kez yer aliyordu. Engelledim (MG)
        resultContents = removeDuplicates(resultContents);

        // Sort contents
        Collections.sort(resultContents, new Comparator() {
            @Override
            public int compare(Object lhs, Object rhs) {
                return ((L_Content) lhs).getContentOrderNo().compareTo(((L_Content) rhs).getContentOrderNo());
            }
        });
        Collections.reverse(resultContents);

        return resultContents;
    }

    public boolean isSelectedCategoriesContainAll(ArrayList<L_Category> categoryList){
        /*
        * Show All secilmeden tum kategoriler tek tek secilmisse
        * */
        int databaseCategorySize = getCategoriesOnlyHaveContent().size();
        if(categoryList.size() == databaseCategorySize || categoryList.size() == databaseCategorySize+1){
            return true;
        } else {
            return false;
        }
    }

    public List<L_Content> removeDuplicates(List<L_Content> l) {
        Set<L_Content> s = new TreeSet<L_Content>(new Comparator<L_Content>() {

            @Override
            public int compare(L_Content o1, L_Content o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });
        s.addAll(l);
        return new ArrayList<L_Content>(s);
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

    public List getAllContents(String searchQuery)
    {
        try {
            if (searchQuery == null) {
                /*
                * TODO burada crash var bunun icin issue actim takip edecez
                * https://fabric.io/galepress/android/apps/ak.detaysoft.yeryuzudergidis/issues/574be8b2ffcdc0425091f437
                * https://github.com/j256/ormlite-android/issues/55
                * http://stackoverflow.com/questions/37540960/cursor-window-allocation-of-2048-kb-failed
                * */
                return contentsDao.queryForAll();
            } else {
                if (contentsQuery == null) {
                    contentsQuery= makeContentsQuery(false);
                }
                String nameParameter = searchQuery == null ? "%%" : "%"+searchQuery.trim()+"%";
                contentsQuery.setArgumentHolderValue(0,nameParameter);
                return contentsDao.query(contentsQuery);
            }
        } catch (SQLException e){
            e.printStackTrace();
            return null;
        }
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

    private PreparedQuery<L_Content> makeContentsQuery(boolean justDownloaded) throws SQLException {
        QueryBuilder<L_Content, Integer> contentObj = contentsDao.queryBuilder();
        contentObj.where().like(L_Content.NAME_FIELD_NAME, new SelectArg());
        return contentObj.prepare();
    }


    private PreparedQuery<L_Content> makeContentsForAllQuery() throws SQLException {
        QueryBuilder<L_ContentCategory, Integer> categoryContent = contentCategoryDao.queryBuilder();
        categoryContent.selectColumns(L_ContentCategory.CONTENT_ID_FIELD_NAME);
        SelectArg userSelectArg = new SelectArg();
        categoryContent.where().eq(L_ContentCategory.CATEGORY_ID_FIELD_NAME, userSelectArg);
        QueryBuilder<L_Content, Integer> contentObj = contentsDao.queryBuilder();
        contentObj.where().in(L_Content.ID_FIELD_NAME, categoryContent);
        contentObj.orderBy("contentOrderNo", false);
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