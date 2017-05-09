package tuc.christos.chaniacitywalk2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import tuc.christos.chaniacitywalk2.data.DataManager;
import tuc.christos.chaniacitywalk2.model.Player;

public class ProfileActivity extends AppCompatActivity {

    DataManager mDataManager;
    Player mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mDataManager = DataManager.getInstance();
        mPlayer = Player.getInstance();

        TextView name = (TextView) findViewById(R.id.user_profile_name);
        TextView email = (TextView) findViewById(R.id.user_profile_short_bio);
        TextView fName = (TextView) findViewById(R.id.tx1);
        TextView lName = (TextView) findViewById(R.id.tx2);
        TextView created = (TextView) findViewById(R.id.tx3);

        name.setText(mPlayer.getUsername());
        email.setText(mPlayer.getEmail());
        fName.setText(mPlayer.getFirstname());
        lName.setText(mPlayer.getLastname());
        created.setText(mPlayer.getCreated());

    }
}
