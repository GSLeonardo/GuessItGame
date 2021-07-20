package com.example.android.guesstheword.screens.game

import android.os.CountDownTimer
import android.text.format.DateUtils
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import java.util.*
import java.util.concurrent.CountedCompleter

private val CORRECT_BUZZ_PATTERN = longArrayOf(100, 100, 100, 100, 100, 100)
private val PANIC_BUZZ_PATTERN = longArrayOf(0, 200)
private val GAME_OVER_BUZZ_PATTERN = longArrayOf(0, 2000)
private val NO_BUZZ_PATTERN = longArrayOf(0)

class GameViewModel: ViewModel() {

    enum class BuzzTypes (val pattern: LongArray) {
        CORRECT(CORRECT_BUZZ_PATTERN),
        GAME_OVER(GAME_OVER_BUZZ_PATTERN),
        COUNTDOWN_PANIC(PANIC_BUZZ_PATTERN),
        NO_BUZZ(NO_BUZZ_PATTERN)
    }

    companion object {
        //This object represents the important times in the game

        //Time when the game should finish
        private const val DONE = 0L

        //Number of milliseconds in a second
        private const val ONE_SECOND = 1000L

        //Total time of the game
        private const val COUNTDOWN_TIME = 10000L

        private const val COUNT_DOWN_PANIC = 2000L
    }

    private val timer: CountDownTimer

    private var _buzzTypes: LongArray = BuzzTypes.NO_BUZZ.pattern
    val buzzTypes: LongArray
        get() = _buzzTypes

    private val _countDownPanic = MutableLiveData<Boolean>()
    val countDownPanic: LiveData<Boolean>
        get() = _countDownPanic

    // The current word
    private val _word = MutableLiveData<String>() //Internal use
    val word: LiveData<String> //External Use
        get() = _word

    // The current score
    // (Mutable means it can be changed outside the viewModel)
    private val _score = MutableLiveData<Int>()
    //This other score, is for outside viewModel use, just working as a getter
    val score: LiveData<Int>
        get() = _score

    //The current Time
    private val _currentTime = MutableLiveData<Long>()
    val currentTime: LiveData<Long>
        get() = _currentTime

    val currentTimeString = Transformations.map(currentTime, { time ->
        DateUtils.formatElapsedTime(time)
    })

    // The list of words - the front of the list is the next word to guess
    private lateinit var wordList: MutableList<String>

    //A boolean representing if the game has finished
    private val _eventGameFinish = MutableLiveData<Boolean>()
    val eventGameFinished: LiveData<Boolean>
        get() = _eventGameFinish

    private val _eventIsCorrect = MutableLiveData<Boolean>()
    val eventIsCorrect: LiveData<Boolean>
        get() = _eventIsCorrect

    init {
        resetList()
        nextWord()
        _score.value = 0

        timer = object : CountDownTimer(COUNTDOWN_TIME, ONE_SECOND) {
            override fun onTick(millisUntilFinished: Long) {
                _currentTime.value = millisUntilFinished / ONE_SECOND
                if (millisUntilFinished <= COUNT_DOWN_PANIC) {
                    _buzzTypes = BuzzTypes.COUNTDOWN_PANIC.pattern
                    _countDownPanic.value = true
                }
            }

            override fun onFinish() {
                _currentTime.value = DONE
                _eventGameFinish.value = true
            }
        }
        timer.start()

    }

    /**
     * Resets the list of words and randomizes the order
     */
    private fun resetList() {
        wordList = mutableListOf(
                "queen",
                "hospital",
                "basketball",
                "cat",
                "change",
                "snail",
                "soup",
                "calendar",
                "sad",
                "desk",
                "guitar",
                "home",
                "railway",
                "zebra",
                "jelly",
                "car",
                "crow",
                "trade",
                "bag",
                "roll",
                "bubble"
        )
        wordList.shuffle()
    }

    /**
     * Moves to the next word in the list
     */
    private fun nextWord() {
        //Select and remove a word from the list
        if (wordList.isEmpty()) {
            resetList()
        }
        _word.value = wordList.removeAt(0)
    }

    fun onSkip() {
        _score.value = (score.value)?.minus(1)
        nextWord()
    }

    fun onCorrect() {
        _score.value = (score.value)?.plus(1)
        _buzzTypes = BuzzTypes.CORRECT.pattern
        _eventIsCorrect.value = true
        nextWord()
    }

    fun onGameFinishComplete() {
        _eventGameFinish.value = false
        _buzzTypes = BuzzTypes.GAME_OVER.pattern
    }

    fun stopBuzz() {
        _eventIsCorrect.value = false
        _buzzTypes = BuzzTypes.NO_BUZZ.pattern
    }

    override fun onCleared() {
        super.onCleared()
        timer.cancel()
    }
}