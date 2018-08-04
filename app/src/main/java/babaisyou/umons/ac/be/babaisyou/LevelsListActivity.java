package babaisyou.umons.ac.be.babaisyou;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Logger;

import be.ac.umons.babaisyou.exceptions.GamedCompletedException;
import be.ac.umons.babaisyou.exceptions.WrongFileFormatException;
import be.ac.umons.babaisyou.game.Level;


public class LevelsListActivity extends AppCompatActivity {

    private static final String TAG = "BBIY_DEBUG";

    private static LevelsListActivity instance;

    private LevelPack levelPack;
    private String[] levels;
    private String[] allLevels;

    private ListAdapter levelsAdapter;

    @Override
    protected void onResume() {
        // Update All levels and refresh screen

        updateLevelsList();


        super.onResume();
    }

    private void updateLevelsList() {
        ListView levelsListView = (ListView) findViewById(R.id.levelsListView);

        levels = getLevels();

        levelsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, allLevels) {
            /*
             * Changes the color of the list item according if it is playable or not
             */
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                TextView tv = (TextView) v; //Assumes we use an ArrayAdapter or similar (Creates TextView)
                if (!allLevels[position].equals(levels[position])) {
                    //v.setBackgroundColor(Color.LTGRAY);
                    tv.setTextColor(Color.LTGRAY);
                }

                return v;
            };
        };

        levelsListView.setAdapter(levelsAdapter);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        instance = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_levels_list);

        ListView levelsListView = (ListView) findViewById(R.id.levelsListView);

        levelPack = new LevelPack();


        allLevels = getAllLevels();

        //Log.w(TAG, Arrays.toString(levels));

        updateLevelsList();


        levelsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String levelToPlay = String.valueOf(adapterView.getItemAtPosition(i));

                if (!levelToPlay.equals("")) {
                    onLevelSelection(levelToPlay);
                }

            }
        });
    }

    public static LevelsListActivity getInstance() {
        return instance;
    }

    public LevelPack getLevelPack() {
        return levelPack;
    }

    private String[] getLevels() {
        return levelPack.getPlayableLevels();
    }

    private String[] getAllLevels() {
        return levelPack.getAllLevels();
    }

    private void onLevelSelection(String levelChosen) {
        if (Arrays.asList(levels).contains(levelChosen)) {
            Intent intent = new Intent(this, LevelActivity.class);
            intent.putExtra("level", levelChosen);
            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), R.string.toast_please_unlock_level_first, Toast.LENGTH_LONG).show();
        }

    }





    /**
     * Représente un pack de niveau hors ligne
     * Un pack est composé de order et les niveaux.
     *
     * @author Thomas Lavend'Homme
     *
     * Classe interne pour avoir accès au dossier assets
     *
     */
    public class LevelPack {

        private final Logger LOGGER =  Logger.getGlobal();

        /**
         * Nom du fichier qui spécifier l'ordre dans lequel jouer les niveaux
         */
        static final String DEFAULT_ORDER_FILENAME = "order";

        /**
         * Nom du fichier qui retiens les parties déjà jouées
         */
        static final String DEFAULT_FINISHED_FILENAME = ".finished";

        /**
         * Niveau actuel.
         */
        private String currentLevel;

        /**
         * Indice du niveau actuel
         */
        private int currentLevelIndex;

        /**
         * Emplacement du niveau actuel
         */
        private String currentLevelsLocation;

        /**
         * Liste contenant tous les niveaux dans l'ordre
         */
        private String[] levelsList;

        /**
         * Stocke le dernier niveau joué
         */
        private LinkedList<String> alreadyPlayedLevels;

        /**
         * Initialise une série de niveaux à l'endroit spécifié.
         * @param location
         */
        public LevelPack() {
            //lecture de la liste des niveaux
            LinkedList<String> listOfLevel = new LinkedList<>();
            alreadyPlayedLevels = new LinkedList<>();

            try (BufferedReader buffer = new BufferedReader(new InputStreamReader(getAssets().open("levels/" + DEFAULT_ORDER_FILENAME ) ))) {
                String line;
                while ((line = buffer.readLine()) != null) {
                    listOfLevel.add(line);
                }
            } catch (IOException e) {
                LOGGER.log(java.util.logging.Level.SEVERE, null, e);
                throw new RuntimeException(e);
            }
            levelsList = listOfLevel.toArray(new String[listOfLevel.size()]);



            // Lecture de la liste des parties déjà jouées
            updatePlayedLevels();

            // Dernier niveau joué :

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(LevelsListActivity.this);
            boolean historyEnabled = sharedPreferences.getBoolean("remember_last_played_level_preference", false);

            if (historyEnabled) {
                SharedPreferences levelHistory = getSharedPreferences("levelHistory", Context.MODE_PRIVATE);
                Set<String> levelsPlayed = levelHistory.getStringSet("lastLevelPlayed", new HashSet<String>());

                for (String level : levelsList) {

                    if (levelsPlayed.contains(level)) {
                        alreadyPlayedLevels.add(level);
                    }

                }

                //Définit le premier niveau.
                if (levelsList.length != 0) {
                    currentLevel = levelsList[0];
                    currentLevelIndex = 0;
                }
            } else {
                alreadyPlayedLevels.add(levelsList[0]);
                //Définit le premier niveau.
                currentLevel = levelsList[0];
                currentLevelIndex = 0;
            }




        }

        private void updatePlayedLevels() {
            alreadyPlayedLevels = new LinkedList<>();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(LevelsListActivity.this);
            boolean historyEnabled = sharedPreferences.getBoolean("remember_last_played_level_preference", false);

            if (historyEnabled) {
                SharedPreferences levelHistory = getSharedPreferences("levelHistory", Context.MODE_PRIVATE);
                Set<String> levelsPlayed = levelHistory.getStringSet("levelHistory", new HashSet<String>());

                Log.w(TAG, "Played Levels: " + levelsPlayed.toString());

                for (String level : levelsList) {

                    if (levelsPlayed.contains(level)) {
                        alreadyPlayedLevels.add(level);
                    }

                }
            }
        }

        /**
         * Renvoie le niveau actuel dans le liste de niveaux
         * @return
         */
        public Level getCurrentLevel() throws WrongFileFormatException {
            try {
                Level level = Level.load(getAssets().open("levels/"+currentLevel));
                level.setLevelName(currentLevel);
                return level;
            } catch (WrongFileFormatException | IOException e) {
                throw new WrongFileFormatException(e);
            }
        }

        /**
         * Permet de chamger le permier niveau
         */
        public void setFirstLevel(String firstLevel) {
            currentLevel = firstLevel;
            for (int i=0; i<levelsList.length; i++) {
                if (levelsList[i].equals(firstLevel)) {
                    currentLevelIndex = i;
                    break;
                }
            }
        }

        /**
         * Passe au niveau suivant, retourne null si la partie est terminée
         * @return Le niveau suivant
         * @throws GamedCompletedException SI le joueur a fini la partie
         */
        public Level nextLevel() throws GamedCompletedException {
            //Ajouter l'élément à la liste des jeux joués et sauver dans le fichier
            if (!alreadyPlayedLevels.contains(currentLevel)) {
                //Ajouter si pas fini dans une session précédente
                alreadyPlayedLevels.add(currentLevel);
            }

            //Save the level played
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(LevelsListActivity.this);
            boolean historyEnabled = sharedPreferences.getBoolean("remember_last_played_level_preference", false);

            if (historyEnabled) {
                // Sauver dernier niveau joué
                SharedPreferences levelHistory = getSharedPreferences("levelHistory", Context.MODE_PRIVATE);
                Set<String> history = levelHistory.getStringSet("levelHistory", new HashSet<String>());

                //Add level
                for (String level : alreadyPlayedLevels ) {
                    history.add(level);
                }

                Log.w(TAG, "Played Levels:"+history.toString());

                //Write to SharedPreferences
                SharedPreferences.Editor editor = levelHistory.edit();
                editor.clear();
                editor.putStringSet("levelHistory", history);
                editor.commit();
            }




            /* Cannot save to the apk
            try (BufferedWriter buffer = new BufferedWriter(new FileOutputStream(context.getFileStreamPath("levels/" + DEFAULT_FINISHED_FILENAME), true))) {
                if (alreadyPlayedLevels.size() != 0) {
                    buffer.write("");
                    for (String level : alreadyPlayedLevels) {
                        buffer.append(level + "\n");
                    }
                }
            } catch (IOException e1) {
                LOGGER.log(java.util.logging.Level.SEVERE, "Problem occured while saving last played Level", e1);
                throw new RuntimeException(e1);
            }
            */


            if (currentLevelIndex + 1 >= levelsList.length) {
                //Cas où le joueur a fini tous les niveaux
                throw new GamedCompletedException();
            }

            // Cas où le joueur n'as pas encore fini
            currentLevelIndex++;
            currentLevel = levelsList[currentLevelIndex];

            try {
                return Level.load(getAssets().open("levels/"+currentLevel));
            } catch (WrongFileFormatException | IOException e) {
                LOGGER.log(java.util.logging.Level.SEVERE, "Problem occured while loading current Level", e);
            }
            //Si le jeu n'as pas pu charger le niveau suivant
            return null;

        }

        /**
         * Renvoie la liste de tous les niveaux jouables, c'est à dire tous les niveaux
         * déjà joués plus le suivant.
         * Si un niveau est jouable, son nom sera dans la liste, sinon La valeur sera null.
         * @return liste de tous les niveaux jouables, liste de String
         *
         */
        public String[] getPlayableLevels() {
            updatePlayedLevels();
            String[] playableLevels = new String[levelsList.length];
            boolean previousFinished = true;
            boolean played = false;
            for (int i=0; i<levelsList.length; i++) {
                played = alreadyPlayedLevels.contains(levelsList[i]);
                if (played) {
                    playableLevels[i] = levelsList[i];
                    previousFinished = true;
                }
                else if (previousFinished) {
                    //Ce niveau n'a jamais été joué mais il est accessible car le précédent est réussi.
                    playableLevels[i] = levelsList[i];
                    previousFinished = false;
                } else {
                    //Valeur par défaut si pas lu
                    playableLevels[i] = "";
                }
            }
            return playableLevels;
        }


        /**
         * Renvoie la liste de tous les niveaux disponibles
         * @return la liste de tous les niveaux jouables
         */
        public String[] getAllLevels() {
            return levelsList;
        }



    }
}
