/*
 *  Copyright (C) 2021 Claus Niesen
 *
 *  This file is part of Claus' Morse Trainer.
 *
 *  Claus' Morse Trainer is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Claus' Morse Trainer is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Claus' Morse Trainer.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.niesens.morsetrainer;

import android.os.AsyncTask;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.List;
import java.util.Random;

public class Trainer extends AsyncTask<Void, Void, Void> {
    private final MorsePlayer morsePlayer;
    private final TextSpeaker textSpeaker;
    private List<Word> wordList;
    private int wordTrainTimes;
    private boolean speakFirst;
    private boolean randomOrder;
    private List<Word> staticWordList;

    private final Random random = new Random();

    Trainer(MorsePlayer morsePlayer, TextSpeaker textSpeaker, List<Word> wordList, int wordTrainTimes, boolean speakFirst, boolean randomOrder) {
        this.morsePlayer = morsePlayer;
        this.textSpeaker = textSpeaker;
        this.wordList = wordList;
        this.wordTrainTimes = wordTrainTimes;
        this.speakFirst = speakFirst;
        this.randomOrder = randomOrder;
        this.staticWordList = wordList;
    }

    public void setWordTrainTimes(int wordTrainTimes) {
        this.wordTrainTimes = wordTrainTimes;
    }

    public void setSpeakFirst(boolean speakFirst) {
        this.speakFirst = speakFirst;
    }

    public void setRandomOrder(boolean randomOrder) {
        this.randomOrder = randomOrder;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected Void doInBackground(Void... params) {

        synchronized (this) {
            Word word = null;
            int wordTrainedCount = 0;
            int wordNumber = 0;
            int wordListSize = wordList.size();
            while (!isCancelled()) {
                if(wordTrainedCount == wordListSize){
                    wordList = staticWordList;
                    word = null;
                    wordTrainedCount = 0;
                    wordNumber = 0;
                }
                if (word == null || wordTrainedCount >= wordTrainTimes) {
                    if (randomOrder) {
                        if (wordTrainedCount > 0){
                            wordNumber++;
                        }
                    } else {
                        wordNumber = random.nextInt(wordList.size());
                    }
                    word = wordList.get(wordNumber);
                    wordList.remove(word);
                    wordNumber--;
                    System.out.println(wordNumber);
                    System.out.println(word.getSpeakText());
                    wordTrainedCount = 0;
                }
                boolean speakAfter = true;
                if (speakFirst) {
                    speakAfter = false;
                    textSpeaker.speak(word.getSpeakText(), this);
                    try {
                        wait();  // wait for text-to-speech to finish
                    } catch (InterruptedException e) {
                        return null;
                    }
                }
                morsePlayer.play(MorseTranslate.textToMorse(word.getMorseText()));
                if (speakAfter) {
                    textSpeaker.speak(word.getSpeakText(), this);
                    try {
                        wait(); // wait for text-to-speech to finish
                    } catch (InterruptedException e) {
                        return null;
                    }
                }
                wordTrainedCount++;
            }
        }
        return null;
    }

}
