package juliusvssphinx.android.nascimento.costa.juliusvssphinx;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    public void initSphinx (View view){
        Intent intent = new Intent(this, SphinxRecognizer.class);
        startActivity(intent);
    }

}
