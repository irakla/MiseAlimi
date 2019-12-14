package com.example.myapplication

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import androidx.work.Data
import androidx.work.testing.TestWorkerBuilder
import androidx.work.ListenableWorker.Result
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Thread.sleep
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.jar.Manifest

@RunWith(AndroidJUnit4::class)
class UploadTest {
    private lateinit var context: Context
    private lateinit var executor: Executor

    @get:Rule
    private val permissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.INTERNET
    )

    @Before
    fun setup(){
        context = ApplicationProvider.getApplicationContext()
        executor = Executors.newSingleThreadExecutor()
    }

    @Test
    fun testUploadWork(){
        val worker = TestWorkerBuilder<PeriodicUploader>(
            context = context
            , executor = executor
            , inputData = Data.EMPTY
        ).build()

        val result = worker.doWork()
        assertThat(result, `is`(Result.success()))

        sleep(2000)
    }
}