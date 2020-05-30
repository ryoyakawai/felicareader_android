package com.ryoyakawai.felicareader.felicalibs

import android.nfc.Tag
import android.nfc.tech.NfcF
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

data class BasicTagInfo(val idm: ByteArray, val mid: ByteArray, val pmm: ByteArray, val systemCode: ByteArray)

class FelicaLibsReader {

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
    fun getBasicInfo00(idm: ByteArray, serviceCode: ByteArray, size: Int): ByteArray {
        val cmd = this.readWithoutEncryption(idm, serviceCode, size)
        nfc.connect()
        val response = this.nfc.transceive(cmd)
        nfc.close()
        return response
    }

    /**
     * Read Without Encryption コマンド取得
     */
    @Throws(IOException::class)
    private fun readWithoutEncryption(idm: ByteArray, serviceCode: ByteArray, size: Int): ByteArray {
        val bout = ByteArrayOutputStream(100)

        bout.write(0)                 // データ長バイトのダミー
        bout.write(0x06)              // コマンド「Read Without Encryption」
        bout.write(idm)                  // IDm 8byte
        bout.write(1)                 // サービスコードリストの長さ(以下２バイトがこの数分繰り返す)
        bout.write(serviceCode[1].toInt())        // 利用履歴のサービスコード下位バイト
        bout.write(serviceCode[0].toInt())        // 利用履歴のサービスコード上位バイト
        bout.write(size)                          // ブロック数
        for (i in 0 until size) {
            bout.write(0x80)                  // ブロックエレメント上位バイト 「Felicaユーザマニュアル抜粋」の4.3項参照
            bout.write(i)                        // ブロック番号
        }

        val msg = bout.toByteArray()
        msg[0] = msg.size.toByte() // 先頭１バイトはデータ長
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