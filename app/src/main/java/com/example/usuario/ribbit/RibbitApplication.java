package com.example.usuario.ribbit;

import android.app.Application;

import com.parse.Parse;

/**
 * Created by usuario on 18/03/2015.
 */
public class RibbitApplication extends Application {

    @Override
    public void onCreate(){
        super.onCreate();
        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        Parse.initialize(this, "Vrpo4GrO8Mm3Wp2ym87YHjPFkFTTXFBqkbXjcTWV", "GiCm64db3cS8WobITheAFgLAhRd1uiw8frxZJ3Ma");

        /*ParseObject testObject = new ParseObject("TestObject");
        testObject.put("foo", "bar");
        testObject.saveInBackground();
        */

    }
}
