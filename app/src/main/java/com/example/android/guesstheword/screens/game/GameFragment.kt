/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.guesstheword.screens.game

import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.getSystemService
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment.findNavController
import com.example.android.guesstheword.R
import com.example.android.guesstheword.databinding.GameFragmentBinding

/**
 * Fragment where the game is played
 */
class GameFragment : Fragment() {

    //Connecting ViewModel with UI fragment
    private lateinit var viewModel: GameViewModel

    private lateinit var binding: GameFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Inflate view and obtain an instance of the binding class
        binding = DataBindingUtil.inflate(
                inflater,
                R.layout.game_fragment,
                container,
                false
        )

        viewModel = ViewModelProvider(this).get(GameViewModel::class.java)

        binding.gameViewModel = viewModel
        binding.setLifecycleOwner(this) //This allows to bind data with layout

        /**
         * Adding Observers for the word and score that work as LiveData
         * Although most of these observers can be automatically added to the layout by data binding
         * This way the UI is updated automatically each time those values change
         */
        /*viewModel.score.observe(this.viewLifecycleOwner, Observer { newScore ->
            binding.scoreText.text = newScore.toString()
        })*/

        /*viewModel.word.observe(this.viewLifecycleOwner, Observer { newWord ->
            binding.wordText.text = newWord
        })*/

        /*viewModel.currentTime.observe(this.viewLifecycleOwner, Observer { newTime ->
            binding.timerText.text = DateUtils.formatElapsedTime(newTime)
        })*/

        viewModel.eventGameFinished.observe(this.viewLifecycleOwner, Observer { hasFinished ->
            if(hasFinished) {
                gameFinished()
                viewModel.onGameFinishComplete()

                buzz(viewModel.buzzTypes)
                viewModel.stopBuzz()
            }
        })

        viewModel.eventIsCorrect.observe(this.viewLifecycleOwner, Observer { isCorrect ->
            if(isCorrect) {
                buzz(viewModel.buzzTypes)
                viewModel.stopBuzz()
            }
        })

        viewModel.countDownPanic.observe(this.viewLifecycleOwner, Observer { panic ->
            if(panic) {
                buzz(viewModel.buzzTypes)
            }
        })

        return binding.root

    }


    /**
     * Called when the game is finished
     */
    private fun gameFinished() {
        val action = GameFragmentDirections.actionGameToScore(viewModel.score.value ?: 0)
        // Having ?: means that in case the value is null, 0 is going to be used instead

        findNavController(this).navigate(action)
    }

    private fun buzz(pattern: LongArray) {
        val buzzer = activity?.getSystemService<Vibrator>()

        buzzer?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                buzzer.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                //deprecated in API 26
                buzzer.vibrate(pattern, -1)
            }
        }
    }


}
