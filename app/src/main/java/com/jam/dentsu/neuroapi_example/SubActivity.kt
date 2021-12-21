package com.jam.dentsu.neuroapi_example

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import kotlinx.android.synthetic.main.activity_sub.*
import neuronicle.EEGGrpc
import neuronicle.NeuroNicleProto
import java.util.*
import kotlin.concurrent.schedule

class SubActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sub)



    }
    private var ch1RawArray: MutableList<Int> = IntArray(250){0}.toMutableList()
    private var ch2RawArray: MutableList<Int> = IntArray(250){0}.toMutableList()

    private val clientId = "sd_kogaku_2021"
    private val serverIP = "18.221.165.249"
    private val port = 80

    private val channel = ManagedChannelBuilder.forAddress(serverIP, port)
        .usePlaintext()
        .build()
    private val stub = EEGGrpc.newStub(channel)

    private var liked = 0
    private var likedsum = 0
    private var LikedLIst = mutableListOf<Int>()

    private val observer = stub.start(object : StreamObserver<NeuroNicleProto.ConversionReply> {
        override fun onNext(reply: NeuroNicleProto.ConversionReply) {
            //emotionLabel.text = "${reply.getDataOrDefault("Like",0)},${reply.getDataOrDefault("Interest",0)},${reply.getDataOrDefault("Concentration",0)},${reply.getDataOrDefault("Calmness",0)},${reply.getDataOrDefault("Stress",0)}"
            liked = reply.getDataOrDefault("Like",0)
            LikedLIst.add(liked)
            Log.d("observer","success")
        }

        override fun onError(t: Throwable) {
            Log.d("observer","error")
        }

        override fun onCompleted() {
            Log.d("observer","complete")
        }
    })

    override fun onDestroy() {
        neuroNicleService.instance.isDestroyed=true
        val message = NeuroNicleProto.FinishRequest.newBuilder().setClientCode(clientId).build()
        stub.finishConnection(message, object : StreamObserver<NeuroNicleProto.FinishReply> {
            override fun onNext(reply: NeuroNicleProto.FinishReply) {
                println(reply.ok)
            }
            override fun onError(t: Throwable) {}

            override fun onCompleted() {
                println("complete")
            }
        })
        super.onDestroy()
    }

    private var dataCount = 1;
    fun onDataReceived(ch1: Int, ch2: Int) {
        ch1RawArray.add(ch1)
        ch2RawArray.add(ch2)
        ch1RawArray.removeAt(0)
        ch2RawArray.removeAt(0)
        if(dataCount==250){
            val message = NeuroNicleProto.ConversionRequest.newBuilder().setClientCode(clientId).addAllCh1(ch1RawArray).addAllCh2(ch2RawArray).build()
            observer.onNext(message)
            dataCount=0
        }
        dataCount+=1
    }

    private fun GetSimData(){
        val ch1 = (0..1000).random()+(9000..10000).random()
        val ch2 = (0..1000).random()+(9000..10000).random()
        ch1RawArray.add(ch1)
        ch2RawArray.add(ch2)
        ch1RawArray.removeAt(0)
        ch2RawArray.removeAt(0)
        if(dataCount==250){
            val message = NeuroNicleProto.ConversionRequest.newBuilder().setClientCode(clientId).addAllCh1(ch1RawArray).addAllCh2(ch2RawArray).build()
            observer.onNext(message)
            dataCount=0
        }
        dataCount+=1
    }

    private fun ChangePic(){
        //画像を10秒表示→5秒インターバル
        //いちいちpic1,pic2などにするのではなく画像を配列として格納できるとindexで管理できそう

    }
}