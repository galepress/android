package ak.detaysoft.galepress.test;

import android.content.Context;
import android.util.Log;

import java.util.List;

import ak.detaysoft.galepress.database_models.L_Category;

/**
 * Created by adem on 25/02/14.
 */
public class Test {
    Context context;

    public Test(Context context) {
        this.context = context;
    }

    public void testIt(){
        DatabaseApi databaseApi = new DatabaseApi(this.context);
        L_Category category = new L_Category(100, "Adem'in test categorysi");
        databaseApi.createCategory(category);



//        databaseApi.create(comment);
        Log.i("Adem TEST", "Test It Working Now Categoriler listeleniyor");
//        Toast.makeText(this.context,"Test It Working Now", Toast.LENGTH_LONG).show();
//        List<Comment> comments = databaseApi.getAll();
//        for (Comment c: comments ){
//            Log.i("Adem TEST", c.getId()+" ) "+ c.getText());
//            Toast.makeText(this.context, c.getId()+" ) "+ c.getText(), Toast.LENGTH_LONG).show();
//        }
        List<L_Category> categories = databaseApi.getAllCategories();
        for (L_Category c: categories){
            Log.i("Adem TEST", c.getCategoryID()+" ) "+ c.getCategoryName());
        }




    }
}
