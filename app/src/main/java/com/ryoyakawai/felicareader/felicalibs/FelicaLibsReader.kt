package com.ryoyakawai.felicareader.felicalibs

import android.nfc.Tag
import android.nfc.tech.NfcF
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

data class BasicTagInfo(val idm: ByteArray, val mid: ByteArray, val pmm: ByteArray, val systemCode: ByteArray)

class FelicaLibsReader {
    private val tTAG = "ryoyakawai_falicareader"

    private lateinit var nfc: NfcF
    private lateinit var idm: ByteArray
    private lateinit var mid: ByteArray
    private lateinit var pmm: ByteArray
    private lateinit var systemCode: ByteArray

    fun getBasicInformation(tag: Tag): BasicTagInfo {
        this.nfc = NfcF.get(tag)

        // idm
        this.idm = tag.id

        // ManufactureID
        this.mid = byteArrayOf(idm[0], idm[1])

        //  pmm
        this.pmm = nfc.manufacturer

        // system code
        this.systemCode  = nfc.systemCode

        return BasicTagInfo(this.idm, this.mid, this.pmm, this.systemCode)
    }

    @Throws(IOException::class)
    fun getAllTransactionHistory(idm: ByteArray, serviceCode: ByteArray): ArrayList<ByteArray> {
        val dataPerTime: Int = 10
        val prefixByte: Int = 2 + 8 + 3
        val recordDataLength: Int = 16
        lateinit var response: ByteArray
        var arrayAllHistory: ArrayList<ByteArray> = arrayListOf<ByteArray>()

        for(i in 0..1) {
            response = this.getTransactionHistory(idm, serviceCode, dataPerTime, i * dataPerTime)
            var responseString = this.bytesToHexString(response)
            Log.d(tTAG, "polling_response=[$responseString]")

            // 履歴を分割
            for(i in 0..10) {
                val startIdx = prefixByte + recordDataLength * i
                val endIdx = prefixByte + recordDataLength * i + recordDataLength
                if(response.size >= endIdx) {
                    val oneRecord = response.copyOfRange(startIdx, endIdx)
                    arrayAllHistory.add(oneRecord)
                }
            }
        }
        (0 until arrayAllHistory.size).forEach { i ->
            val oneRecordString = this.bytesToHexString(arrayAllHistory[i])
            Log.d(tTAG, "OneRecord=[$oneRecordString]")
        }
        return arrayAllHistory
    }

    @Throws(IOException::class)
    fun getTransactionHistory(idm: ByteArray, serviceCode: ByteArray, size: Int, offset:Int): ByteArray {
        val cmd = this.readWithoutEncryption(idm, serviceCode, size, offset)
        nfc.connect()
        val response = this.nfc.transceive(cmd)
        nfc.close()
        return response
    }

    /**
     * Read Without Encryption コマンド取得
     */
    @Throws(IOException::class)
    private fun readWithoutEncryption(idm: ByteArray, serviceCode: ByteArray, size: Int, offset: Int): ByteArray {
        val bout = ByteArrayOutputStream(100)

        bout.write(0x00)                 // データ長バイトのダミー
        bout.write(0x06)                 // コマンド「Read Without Encryption」
        bout.write(idm)                     // IDm 8byte
        bout.write(0x01)                 // サービスコードリストの長さ(以下２バイトがこの数分繰り返す)
        bout.write(serviceCode[1].toInt())  // 利用履歴のサービスコード下位バイト
        bout.write(serviceCode[0].toInt())  // 利用履歴のサービスコード上位バイト
        bout.write(size)                    // ブロック数
        for (i in 0 until size) {
            bout.write(0x80)             // ブロックエレメント上位バイト 「Felicaユーザマニュアル抜粋」の4.3項参照
            bout.write(i + offset)
            // ブロック番号
        }

        val msg = bout.toByteArray()
        msg[0] = msg.size.toByte()          // 先頭１バイトはデータ長
        return msg
    }

    @Throws(IOException::class)
    fun bytesToHexString(bytes: ByteArray): String? {
        val sb = java.lang.StringBuilder()
        val formatter = Formatter(sb)
        for (b in bytes) {
            formatter.format("%02x", b)
        }
        return sb.toString().toUpperCase(Locale.getDefault())
    }
}