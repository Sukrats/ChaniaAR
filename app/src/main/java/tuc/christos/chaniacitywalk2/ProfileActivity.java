package tuc.christos.chaniacitywalk2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import tuc.christos.chaniacitywalk2.data.DataManager;
import tuc.christos.chaniacitywalk2.model.Player;

public class ProfileActivity extends AppCompatActivity {

    DataManager mDataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mDataManager = DataManager.getInstance();
        if(!mDataManager.isInstantiated())
            mDataManager.init(this);

        TextView tx = (TextView) findViewById(R.id.user_profile_name);
    }
}
