/* ====================================================================
 * Copyright (c) 2014 Alpha Cephei Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY ALPHA CEPHEI INC. ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL CARNEGIE MELLON UNIVERSITY
 * NOR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 */

package costanascimento.android.pocketsphinx.demo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

public class PocketSphinxActivity extends Activity implements
        RecognitionListener {

    private static final String PORTUGUESE_RECOGNIZER = "portuguese_recognizer";
    private static final String ENGLISH_RECOGNIZER = "english_recognizer";

    private static final String RESOURCES_LOCATION = "models";
    private static final String ENGLISH_AM_LOCATION = "hmm/en-us-semi";
    private static final String ENGLISH_DICT_LOCATION = "dict/cmu07a.dic";
    private static final String ENGLISH_GRAM_LOCATION = "grammar/digits.gram";
    private static final String PORTUGUESE_AM_LOCATION = "am";
    private static final String PORTUGUESE_DICT_LOCATION = "dict/constituicao.dic";
    private static final String PORTUGUESE_GRAM_LOCATION = "grammar/digitos.gram";


    private String currentSearch;
    private SpeechRecognizer recognizer;
    private HashMap<String, Integer> captions;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        // Prepare the data for UI
        captions = new HashMap<String, Integer>();
        captions.put(ENGLISH_RECOGNIZER, R.string.english_recognizer_caption);
        captions.put(PORTUGUESE_RECOGNIZER, R.string.portuguese_recognizer_caption);
        setContentView(R.layout.main);
        ((TextView) findViewById(R.id.caption_text))
                .setText(R.string.no_recognizer);

        Button button = (Button) findViewById(R.id.speech_button);
        button.setEnabled(false);
        button.setOnClickListener(onClickListener);

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radiogroup);
        radioGroup.setOnCheckedChangeListener(onCheckedChangeListener);
        radioGroup.setEnabled(false);
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
            ((TextView) findViewById(R.id.result_text)).setText(text);
        } else {
            ((TextView) findViewById(R.id.result_text)).setText(R.string.no_hyphotesis);
        }
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    /**
     * Método chamado quando o reconhecedor detecta silêncio entra as falas.
     */
    @Override
    public void onEndOfSpeech() {
    }

    private void switchSearch(String searchName) {
        currentSearch = searchName;
        String caption = getResources().getString(captions.get(searchName));
        ((TextView) findViewById(R.id.caption_text)).setText(caption);
    }

    private void setupRecognizer(File assetsDir, Integer recognizerId) {
        File modelsDir = new File(assetsDir, RESOURCES_LOCATION);

        if (recognizerId == R.id.english_recognizer_radio) {
            recognizer = defaultSetup()
                    .setAcousticModel(new File(modelsDir, ENGLISH_AM_LOCATION))
                    .setDictionary(new File(modelsDir, ENGLISH_DICT_LOCATION))
                    .setRawLogDir(assetsDir).setKeywordThreshold(1e-20f)
                    .getRecognizer();
            recognizer.addListener(this);

            File englishGrammar = new File(modelsDir, ENGLISH_GRAM_LOCATION);
            recognizer.addGrammarSearch(ENGLISH_RECOGNIZER, englishGrammar);
        } else {
            recognizer = defaultSetup()
                    .setAcousticModel(new File(modelsDir, PORTUGUESE_AM_LOCATION))
                    .setDictionary(new File(modelsDir, PORTUGUESE_DICT_LOCATION))
                    .setRawLogDir(assetsDir).setKeywordThreshold(1e-20f)
                    .getRecognizer();
            recognizer.addListener(this);

            File portugueseGrammar = new File(modelsDir, PORTUGUESE_GRAM_LOCATION);
            recognizer.addGrammarSearch(PORTUGUESE_RECOGNIZER, portugueseGrammar);
        }
    }

    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        private boolean isRecording = false;

        @Override
        public void onClick(View v) {
            if (!isRecording) {
                isRecording = true;
                ((Button) findViewById(R.id.speech_button)).setText(R.string.recording_button);
                recognizer.startListening(currentSearch);

            } else {
                isRecording = false;
                ((Button) findViewById(R.id.speech_button)).setText(R.string.record_button);
                recognizer.stop();
                recognizer.cancel();
            }
        }
    };

    private final RadioGroup.OnCheckedChangeListener onCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        private boolean isRecording = false;

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            // Recognizer initialization is a time-consuming and it involves IO,
            // so we execute it in async task
            new RecognizerInitializer(PocketSphinxActivity.this).execute(checkedId);

            if (checkedId == R.id.english_recognizer_radio) {
                switchSearch(ENGLISH_RECOGNIZER);
            } else {
                switchSearch(PORTUGUESE_RECOGNIZER);
            }
            findViewById(R.id.speech_button).setEnabled(true);
        }
    };

    private class RecognizerInitializer extends AsyncTask<Integer, Void, Exception> {

        private ProgressDialog progressDialog;
        private Context context;

        public RecognizerInitializer(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage(PocketSphinxActivity.this.getString(R.string.recognizer_setup));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
        }

        @Override
        protected Exception doInBackground(Integer... params) {
            try {
                Assets assets = new Assets(PocketSphinxActivity.this);
                File assetDir = assets.syncAssets();
                setupRecognizer(assetDir, params[0]);
            } catch (IOException e) {
                return e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Exception result) {
            progressDialog.dismiss();
            if (result != null) {
                ((TextView) findViewById(R.id.caption_text))
                        .setText(R.string.recognizer_setup_failed);
                Log.e("SphinxError", result.getStackTrace().toString());
            }
        }
    }


}
