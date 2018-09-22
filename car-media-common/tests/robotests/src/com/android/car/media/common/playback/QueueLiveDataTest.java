/*
 * Copyright 2018 The Android Open Source Project
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

package com.android.car.media.common.playback;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Lifecycle;

import com.android.car.arch.common.testing.CaptureObserver;
import com.android.car.arch.common.testing.TestLifecycleOwner;
import com.android.car.media.common.TestConfig;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Collections;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = TestConfig.MANIFEST_PATH, sdk = TestConfig.SDK_VERSION)
public class QueueLiveDataTest {

    @Rule
    public final MockitoRule mMockitoRule = MockitoJUnit.rule();
    @Rule
    public final InstantTaskExecutorRule mTaskExecutorRule = new InstantTaskExecutorRule();
    @Rule
    public final TestLifecycleOwner mLifecycleOwner = new TestLifecycleOwner();

    @Mock
    private MediaControllerCompat mMediaController;
    @Captor
    private ArgumentCaptor<MediaControllerCompat.Callback> mCapturedCallback;

    private QueueLiveData mQueueLiveData;

    @Before
    public void setUp() {
        doNothing().when(mMediaController).registerCallback(mCapturedCallback.capture());
        mQueueLiveData = new QueueLiveData(mMediaController);
    }

    @Test
    public void testRegistersOnActive() {
        mQueueLiveData.observe(mLifecycleOwner, new CaptureObserver<>());

        assertThat(mCapturedCallback.getValue()).isNotNull();
    }

    @Test
    public void testGetValueOnActive() {
        List<MediaSessionCompat.QueueItem> queue = Collections.emptyList();
        when(mMediaController.getQueue()).thenReturn(queue);
        CaptureObserver<List<MediaSessionCompat.QueueItem>> observer = new CaptureObserver<>();
        mQueueLiveData.observe(mLifecycleOwner, observer);

        assertThat(observer.getObservedValue()).isEqualTo(queue);
    }

    @Test
    public void testDeliversValueToObserver() {
        CaptureObserver<List<MediaSessionCompat.QueueItem>> observer = new CaptureObserver<>();
        mQueueLiveData.observe(mLifecycleOwner, observer);
        List<MediaSessionCompat.QueueItem> queue = Collections.emptyList();

        mCapturedCallback.getValue().onQueueChanged(queue);

        assertThat(observer.hasBeenNotified()).isTrue();
        assertThat(observer.getObservedValue()).isEqualTo(queue);
    }

    @Test
    public void testUnregistersOnInactive() {
        mQueueLiveData.observe(mLifecycleOwner, new CaptureObserver<>());
        mLifecycleOwner.markState(Lifecycle.State.DESTROYED);

        verify(mMediaController).unregisterCallback(mCapturedCallback.getValue());
    }

}
