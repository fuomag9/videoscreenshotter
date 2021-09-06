package com.fuomag9.videoscreenshotter

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.MediaMetadataRetriever.OPTION_CLOSEST
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.documentfile.provider.DocumentFile
import com.fuomag9.videoscreenshotter.ui.theme.VideoScreenshotterTheme
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import android.content.Intent





class MainActivity : ComponentActivity() {
    companion object CompanionStuff {
        lateinit var Uri: MutableState<Uri?>
        lateinit var exoplayer: SimpleExoPlayer
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent
        val action = intent.action
        var uri : Uri? = null

        if (Intent.ACTION_VIEW == action) {
             uri = intent.data
        }


        setContent {
            VideoScreenshotterTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    MainFunction(uri)
                }
            }
        }
    }
}


@Composable
fun MainFunction(VideoUri: Uri? = null) {
    val context = LocalContext.current
    //initialize here or it will complain inside the functions :(
    MainActivity.Uri =
        rememberSaveable() { mutableStateOf<Uri?>(VideoUri) }
    MainActivity.exoplayer = remember(context) {
        SimpleExoPlayer.Builder(context).build().apply {
        }
    }
    Column() {
        VideoPlayer(MainActivity.exoplayer, MainActivity.Uri)
        OpenDocumentPicker()
    }

}

@Composable
private fun OpenDocumentPicker() {
    val context = LocalContext.current

    val resultwrite = remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        MainActivity.Uri.value = it
    }
    val launchercreate =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument()) {
            resultwrite.value = it
        }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(
            onClick = { launcher.launch(arrayOf("video/*")) },
            modifier = Modifier.padding(top = 20.dp)
        ) {
            Text(text = "Open video")
        }

        //Todo: get recycle video filename here
        Button(
            onClick = {
                val documentFile = DocumentFile.fromSingleUri(context, MainActivity.Uri.value!!)
                var filename = documentFile!!.name?.substringBeforeLast(".")
                filename += "-" + MainActivity.exoplayer.currentPosition.toString() + ".png";
                launchercreate.launch(filename)
            },
            modifier = Modifier.padding(top = 20.dp)
        ) {
            Text(text = "Save screenshot")
        }
    }



    MainActivity.Uri.value?.let { videoUri ->

        MainActivity.exoplayer.pause() //pause video if it's playing

        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, videoUri)

        // Get a frame in Bitmap by specifying time.
        // Be aware that the parameter must be in "microseconds", not milliseconds.
        val bitmap =
            retriever.getFrameAtTime(MainActivity.exoplayer.currentPosition * 1000, OPTION_CLOSEST)



        resultwrite.value?.let {
            val contentResolver = context.contentResolver
            fun imageToBitmap(bitmap: Bitmap): ByteArray {
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
                return stream.toByteArray()
            }

            fun alterDocument(uri: Uri) {
                try {
                    contentResolver.openFileDescriptor(uri, "w")?.use { parcelFileDescriptor ->
                        FileOutputStream(parcelFileDescriptor.fileDescriptor).use {
                            it.write(
                                bitmap?.let { bitmap -> imageToBitmap(bitmap) }
                            )
                        }
                    }
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            resultwrite.value?.let { alterDocument(it) }
        }


    }

}

@Composable
fun VideoPlayer(player2: SimpleExoPlayer, video: MutableState<Uri?>) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .size(width = 400.dp, height = 600.dp)
            .fillMaxSize()
    ) {
        // Do not recreate the player everytime this Composable commits


        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
            context,
            Util.getUserAgent(context, context.packageName)
        )

        val source = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(
                Uri.parse(
                    video.value.toString()
                )
            )

        player2.prepare(source)

        AndroidView(factory = { it2 ->
            PlayerView(it2).apply {
                player = player2
            }
        })


    }
}



