package juliusvssphinx.android.nascimento.costa.juliusvssphinx;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

/**
 * Created by Renan on 23/04/2015.
 */
public class SphinxRecognizer extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, RecognitionListener {

    private static final String PORTUGUESE_RECOGNIZER = "portuguese_recognizer";
    private static final String RESOURCES_LOCATION = "models";
    private static final String PORTUGUESE_AM_LOCATION = "am";
    private static final String PORTUGUESE_DICT_LOCATION = "dict/constituicao.dic";
    private static final String PORTUGUESE_GRAM_LOCATION = "grammar/OLD_palavras.gram";


    private BufferedReader bufferedReader;
    private int quantityReadWords;
    private SpeechRecognizer recognizer;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.recognizer);

        /*
        * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
        */
        NavigationDrawerFragment mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        Button recordButton = (Button) findViewById(R.id.record_button);
        recordButton.setOnClickListener(onClickListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        new RecognizerInitializer(this).execute();
    }

    public void onSectionAttached(int number) {
        changeSetWords(number);
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
            case 4:
                mTitle = getString(R.string.title_section4);
                break;
            case 5:
                mTitle = getString(R.string.title_section5);
                break;
            case 6:
                mTitle = getString(R.string.title_section6);
                break;
            case 7:
                mTitle = getString(R.string.title_section7);
                break;
            case 8:
                mTitle = getString(R.string.title_section8);
                break;
            case 9:
                mTitle = getString(R.string.title_section9);
                break;
            case 10:
                mTitle = getString(R.string.title_section10);
                break;
        }
    }

    private void changeSetWords(int number) {
        InputStream inputStream = getResources().openRawResource(R.raw.conjutopalavras);
        bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        quantityReadWords = 1;
        try {
            for (int i = 0; i < (number - 1) * 17; i++) {
                bufferedReader.readLine();
            }

            ((TextView) findViewById(R.id.palavraAtual)).setText(bufferedReader.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void nextWord(View view) {
        try {
            quantityReadWords++;
            if (quantityReadWords >= 18) {
                ((TextView) findViewById(R.id.palavraAtual)).setText(R.string.no_more_words);
            } else {
                ((TextView) findViewById(R.id.palavraAtual)).setText(bufferedReader.readLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onEndOfSpeech() {

    }

    /**
     * Método chamado sempre que uma nova palavra é reconhecida.
     *
     * @param hypothesis palavra reconhecida.
     */
    @Override
    public void onPartialResult(Hypothesis hypothesis) {
    }

    /**
     * Metodo chamado após a chamada 'recognizer.stop();'.
     *
     * @param hypothesis palavra reconhecida.
     */
    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis != null) {

            String text = hypothesis.getHypstr();
            ((TextView) findViewById(R.id.recognized_Word)).setText(text);
        } else {
            ((TextView) findViewById(R.id.recognized_Word)).setText(R.string.no_hyphotesis);
        }
    }

    private void setupRecognizer(File assetsDir) {
        File modelsDir = new File(assetsDir, RESOURCES_LOCATION);

        recognizer = defaultSetup()
                .setAcousticModel(new File(modelsDir, PORTUGUESE_AM_LOCATION))
                .setDictionary(new File(modelsDir, PORTUGUESE_DICT_LOCATION))
                .setRawLogDir(assetsDir).setKeywordThreshold(1e-20f)
                .getRecognizer();
        recognizer.addListener(this);

        File portugueseGrammar = new File(modelsDir, PORTUGUESE_GRAM_LOCATION);
        recognizer.addGrammarSearch(PORTUGUESE_RECOGNIZER, portugueseGrammar);
    }

    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        private boolean isRecording = false;

        @Override
        public void onClick(View v) {
            if (!isRecording) {
                isRecording = true;
                ((Button) findViewById(R.id.record_button)).setText(R.string.record_button_on);
                recognizer.startListening(PORTUGUESE_RECOGNIZER);

            } else {
                isRecording = false;
                ((Button) findViewById(R.id.record_button)).setText(R.string.record_button_off);
                recognizer.stop();
                recognizer.cancel();
            }
        }
    };

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    private class RecognizerInitializer extends AsyncTask<Void, Void, Exception> {

        private ProgressDialog progressDialog;
        private Context context;

        public RecognizerInitializer(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage(SphinxRecognizer.this.getString(R.string.setup_message));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
        }

        @Override
        protected Exception doInBackground(Void... params) {
            try {
                Assets assets = new Assets(SphinxRecognizer.this);
                File assetDir = assets.syncAssets();
                setupRecognizer(assetDir);
            } catch (IOException e) {
                return e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Exception result) {
            progressDialog.dismiss();
            if (result != null) {

                Log.e("SphinxError", result.getStackTrace().toString());
            }
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((SphinxRecognizer) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }
}
