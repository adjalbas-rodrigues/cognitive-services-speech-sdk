package com.microsoft.cognitiveservices.speech.translation;
//
// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE.md file in the project root for full license information.
//

import java.util.ArrayList;
import java.util.concurrent.Future;

import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.microsoft.cognitiveservices.speech.KeywordRecognitionModel;
import com.microsoft.cognitiveservices.speech.ParameterCollection;
import com.microsoft.cognitiveservices.speech.RecognitionErrorEventArgs;
import com.microsoft.cognitiveservices.speech.RecognizerParameterNames;
import com.microsoft.cognitiveservices.speech.util.EventHandlerImpl;
import com.microsoft.cognitiveservices.speech.util.Contracts;

//
//Copyright (c) Microsoft. All rights reserved.
//Licensed under the MIT license. See LICENSE.md file in the project root for full license information.
//

 /**
   * Performs translation on the speech input.
   */
 public final class TranslationRecognizer extends com.microsoft.cognitiveservices.speech.Recognizer
 {
     /**
       * The event IntermediateResultReceived signals that an intermediate recognition result is received.
       */
     public final EventHandlerImpl<TranslationTextResultEventArgs> IntermediateResultReceived = new EventHandlerImpl<TranslationTextResultEventArgs>();

     /**
       * The event FinalResultReceived signals that a final recognition result is received.
       */
     public final EventHandlerImpl<TranslationTextResultEventArgs> FinalResultReceived = new EventHandlerImpl<TranslationTextResultEventArgs>();

     /**
       * The event RecognitionErrorRaised signals that an error occurred during recognition.
       */
     public final EventHandlerImpl<RecognitionErrorEventArgs> RecognitionErrorRaised = new EventHandlerImpl<RecognitionErrorEventArgs>();

     /**
       * The event SynthesisResultReceived signals that a translation synthesis result is received.
       */
     public final EventHandlerImpl<TranslationSynthesisResultEventArgs> SynthesisResultReceived = new EventHandlerImpl<TranslationSynthesisResultEventArgs>();

    /**
      * Initializes an instance of the TranslationRecognizer.
      * @param recoImpl The internal recognizer implementation.
      * @param audioInput An optional audio input configuration associated with the recognizer
      */
     public TranslationRecognizer(com.microsoft.cognitiveservices.speech.internal.TranslationRecognizer recoImpl, AudioConfig audioInput) {
        super(audioInput);

        Contracts.throwIfNull(recoImpl, "recoImpl");
         this.recoImpl = recoImpl;

         intermediateResultHandler = new ResultHandlerImpl(this, /*isFinalResultHandler:*/ false);
         recoImpl.getIntermediateResult().AddEventListener(intermediateResultHandler);

         finalResultHandler = new ResultHandlerImpl(this, /*isFinalResultHandler:*/ true);
         recoImpl.getFinalResult().AddEventListener(finalResultHandler);

         synthesisResultHandler = new SynthesisHandlerImpl(this);
         recoImpl.getTranslationSynthesisResultEvent().AddEventListener(synthesisResultHandler);

         errorHandler = new ErrorHandlerImpl(this);
         recoImpl.getCanceled().AddEventListener(errorHandler);

         recoImpl.getSessionStarted().AddEventListener(sessionStartedHandler);
         recoImpl.getSessionStopped().AddEventListener(sessionStoppedHandler);
         recoImpl.getSpeechStartDetected().AddEventListener(speechStartDetectedHandler);
         recoImpl.getSpeechEndDetected().AddEventListener(speechEndDetectedHandler);

         _Parameters = new ParameterCollection<TranslationRecognizer>(this);
     }

     /**
       * Gets the language name that was set when the recognizer was created.
       * @return Gets the language name that was set when the recognizer was created.
       */
     public String getSourceLanguage() {
         return _Parameters.getString(RecognizerParameterNames.TranslationFromLanguage);
     }

     /**
       * Gets target languages for translation that were set when the recognizer was created.
       * The language is specified in BCP-47 format. The translation will provide translated text for each of language.
       * @return Gets target languages for translation that were set when the recognizer was created.
       */
     public ArrayList<String> getTargetLanguages() {
         return new ArrayList<String>(_TargetLanguages);
     }     // { get; }
     ArrayList<String> _TargetLanguages = new ArrayList<String>();

     /**
       * Gets the name of output voice.
       * @return the name of output voice.
       */
     public String getOutputVoiceName() {
        return _Parameters.getString(RecognizerParameterNames.TranslationVoice);
     }

     /**
       * The collection of parameters and their values defined for this TranslationRecognizer.
       * @return The collection of parameters and their values defined for this TranslationRecognizer.
       */
     public ParameterCollection<TranslationRecognizer> getParameters() {
         return _Parameters;
     }// { get; }
     private ParameterCollection<TranslationRecognizer> _Parameters;

     /**
       * Starts recognition and translation, and stops after the first utterance is recognized. The task returns the translation text as result.
       * Note: RecognizeAsync() returns when the first utterance has been recognized, so it is suitableonly for single shot recognition like command or query. For long-running recognition, use StartContinuousRecognitionAsync() instead.
       * @return A task representing the recognition operation. The task returns a value of TranslationTextResult.
       */
     public Future<TranslationTextResult> recognizeAsync() {
         return s_executorService.submit(() -> {
                 return new TranslationTextResult(recoImpl.Recognize());
             });
     }

     /**
       * Starts recognition and translation on a continuous audio stream, until StopContinuousRecognitionAsync() is called.
       * User must subscribe to events to receive translation results.
       * @return A task representing the asynchronous operation that starts the recognition.
       */
     public Future<Void> startContinuousRecognitionAsync() {
         return s_executorService.submit(() -> {
                 recoImpl.StartContinuousRecognition();
                 return null;
             });
     }

     /**
       * Stops continuous recognition and translation.
       * @return A task representing the asynchronous operation that stops the translation.
       */
     public Future<Void> stopContinuousRecognitionAsync() {
         return s_executorService.submit(() -> {
                 recoImpl.StopContinuousRecognition();
                 return null;
             });
     }

    /**
      * Starts speech recognition on a continuous audio stream with keyword spotting, until stopKeywordRecognitionAsync() is called.
      * User must subscribe to events to receive recognition results.
      * Note: Key word spotting functionality is only available on the Cognitive Services Device SDK. This functionality is currently not included in the SDK itself.
      * @param model The keyword recognition model that specifies the keyword to be recognized.
      * @return A task representing the asynchronous operation that starts the recognition.
      */
    public Future<Void> startKeywordRecognitionAsync(KeywordRecognitionModel model) {
        Contracts.throwIfNull(model, "model");

        return s_executorService.submit(() -> {
                recoImpl.StartKeywordRecognition(model.getModelImpl());
                return null;
            });
    }

    /**
      * Stops continuous speech recognition.
      * Note: Key word spotting functionality is only available on the Cognitive Services Device SDK. This functionality is currently not included in the SDK itself.
      * @return A task representing the asynchronous operation that stops the recognition.
      */
    public Future<Void> stopKeywordRecognitionAsync() {
        return s_executorService.submit(() -> {
                recoImpl.StopKeywordRecognition();
                return null;
            });
    }

     @Override
     protected void dispose(boolean disposing)
     {
         if (disposed)
         {
             return;
         }

         if (disposing)
         {
             recoImpl.getIntermediateResult().RemoveEventListener(intermediateResultHandler);
             recoImpl.getFinalResult().RemoveEventListener(finalResultHandler);
             recoImpl.getCanceled().RemoveEventListener(errorHandler);
             recoImpl.getSessionStarted().RemoveEventListener(sessionStartedHandler);
             recoImpl.getSessionStopped().RemoveEventListener(sessionStoppedHandler);
             recoImpl.getSpeechStartDetected().RemoveEventListener(speechStartDetectedHandler);
             recoImpl.getSpeechEndDetected().RemoveEventListener(speechEndDetectedHandler);
             recoImpl.getTranslationSynthesisResultEvent().RemoveEventListener(synthesisResultHandler);

             intermediateResultHandler.delete();
             finalResultHandler.delete();
             errorHandler.delete();
             recoImpl.delete();
             _Parameters.close();
             disposed = true;
             super.dispose(disposing);
         }
     }

     // TODO should only be visible to parameter collection
     public com.microsoft.cognitiveservices.speech.internal.TranslationRecognizer getRecoImpl() {
         return recoImpl;
     }
     
     private final com.microsoft.cognitiveservices.speech.internal.TranslationRecognizer recoImpl;
     private ResultHandlerImpl intermediateResultHandler;
     private ResultHandlerImpl finalResultHandler;
     private SynthesisHandlerImpl synthesisResultHandler;
     private ErrorHandlerImpl errorHandler;
     private boolean disposed = false;

     // Defines an internal class to raise an event for intermediate/final result when a corresponding callback is invoked by the native layer.
     private class ResultHandlerImpl extends com.microsoft.cognitiveservices.speech.internal.TranslationTexEventListener
     {
         public ResultHandlerImpl(TranslationRecognizer recognizer, boolean isFinalResultHandler)
         {
            Contracts.throwIfNull(recognizer, "recognizer");

             this.recognizer = recognizer;
             this.isFinalResultHandler = isFinalResultHandler;
         }

         @Override
         public void Execute(com.microsoft.cognitiveservices.speech.internal.TranslationTextResultEventArgs eventArgs)
         {
            Contracts.throwIfNull(eventArgs, "eventArgs");

             if (recognizer.disposed)
             {
                 return;
             }

             TranslationTextResultEventArgs resultEventArg = new TranslationTextResultEventArgs(eventArgs);
             EventHandlerImpl<TranslationTextResultEventArgs>  handler = isFinalResultHandler ? recognizer.FinalResultReceived : recognizer.IntermediateResultReceived;
             if (handler != null)
             {
                 handler.fireEvent(this.recognizer, resultEventArg);
             }
         }

         private TranslationRecognizer recognizer;
         private boolean isFinalResultHandler;
     }

     // Defines an internal class to raise an event for error during recognition when a corresponding callback is invoked by the native layer.
     class ErrorHandlerImpl extends com.microsoft.cognitiveservices.speech.internal.TranslationTexEventListener {
         public ErrorHandlerImpl(TranslationRecognizer recognizer) {
            Contracts.throwIfNull(recognizer, "recognizer");

             this.recognizer = recognizer;
         }

         @Override
         public void Execute(com.microsoft.cognitiveservices.speech.internal.TranslationTextResultEventArgs eventArgs)
         {
            Contracts.throwIfNull(eventArgs, "eventArgs");

             if (recognizer.disposed)
             {
                 return;
             }

             RecognitionErrorEventArgs resultEventArg = null; // new RecognitionErrorEventArgs(eventArgs.SessionId, eventArgs.Result.Reason);
             EventHandlerImpl<RecognitionErrorEventArgs> handler = this.recognizer.RecognitionErrorRaised;

             if (handler != null)
             {
                 handler.fireEvent(this.recognizer, resultEventArg);
             }
         }

         private TranslationRecognizer recognizer;
     }

     // Defines an internal class to raise an event for intermediate/final result when a corresponding callback is invoked by the native layer.
     private class SynthesisHandlerImpl extends com.microsoft.cognitiveservices.speech.internal.TranslationSynthesisEventListener
     {
         public SynthesisHandlerImpl(TranslationRecognizer recognizer)
         {
            Contracts.throwIfNull(recognizer, "recognizer");

             this.recognizer = recognizer;
         }

             
         @Override
         public void Execute(com.microsoft.cognitiveservices.speech.internal.TranslationSynthesisResultEventArgs eventArgs)
         {
            Contracts.throwIfNull(eventArgs, "eventArgs");

             if (recognizer.disposed)
             {
                 return;
             }

             TranslationSynthesisResultEventArgs resultEventArg = new TranslationSynthesisResultEventArgs(eventArgs);
             EventHandlerImpl<TranslationSynthesisResultEventArgs> handler = recognizer.SynthesisResultReceived;
             if (handler != null)
             {
                 handler.fireEvent(this.recognizer, resultEventArg);
             }
         }

         private TranslationRecognizer recognizer;
     }
 }
