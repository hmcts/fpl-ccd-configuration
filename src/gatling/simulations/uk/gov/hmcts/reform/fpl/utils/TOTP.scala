package uk.gov.hmcts.reform.fpl.utils

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import jodd.util.Base32

import scala.math.{BigInt, pow}

object TOTP {
  private val codeLength = 6

  final def getPassword(secret: String): String = {
    val timeWindow: Long = System.currentTimeMillis() / 30.seconds.toMillis

    val input: Array[Byte] = BigInt(timeWindow).toByteArray.reverse.padTo(8, 0.toByte).reverse

    val hash: Array[Byte] = calculateHash(Base32.decode(secret), input)
    val offset: Int = hash(hash.length - 1) & 0xf
    val binary: Long = ((hash(offset) & 0x7f) << 24) |
      ((hash(offset + 1) & 0xff) << 16) |
      ((hash(offset + 2) & 0xff) << 8 |
        (hash(offset + 3) & 0xff))

    val otp: Long = binary % pow(10, codeLength).toLong
    ("0" * codeLength + otp.toString).takeRight(codeLength)
  }

  private def calculateHash(key: Array[Byte], input: Array[Byte]): Array[Byte] = {
    val hmac: Mac = Mac.getInstance("HmacSHA1")
    hmac.init(new SecretKeySpec(key, "RAW"))
    hmac.doFinal(input)
  }
}
