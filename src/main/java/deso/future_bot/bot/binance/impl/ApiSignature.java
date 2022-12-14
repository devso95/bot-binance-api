package deso.future_bot.bot.binance.impl;

import deso.future_bot.bot.binance.constant.BinanceApiConstants;
import deso.future_bot.bot.binance.exception.BinanceApiException;
import deso.future_bot.bot.binance.impl.utils.UrlParamsBuilder;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

class ApiSignature {

    static final String op = "op";
    static final String opValue = "auth";
    private static final String signatureMethodValue = "HmacSHA256";
    public static final String signatureVersionValue = "2";

    void createSignature(String accessKey, String secretKey, UrlParamsBuilder builder) {

        if (accessKey == null || "".equals(accessKey) || secretKey == null || "".equals(secretKey)) {
            throw new BinanceApiException(BinanceApiException.KEY_MISSING, "API key and secret key are required");
        }

        builder.putToUrl("recvWindow", Long.toString(100000000))
                .putToUrl("timestamp", Long.toString(System.currentTimeMillis()));

        Mac hmacSha256;
        try {
            hmacSha256 = Mac.getInstance(signatureMethodValue);
            SecretKeySpec secKey = new SecretKeySpec(secretKey.getBytes(), signatureMethodValue);
            hmacSha256.init(secKey);
        } catch (NoSuchAlgorithmException e) {
            throw new BinanceApiException(BinanceApiException.RUNTIME_ERROR,
                    "[Signature] No such algorithm: " + e.getMessage());
        } catch (InvalidKeyException e) {
            throw new BinanceApiException(BinanceApiException.RUNTIME_ERROR,
                    "[Signature] Invalid key: " + e.getMessage());
        }
        String payload = builder.buildSignature();
        String actualSign = new String(Hex.encodeHex(hmacSha256.doFinal(payload.getBytes())));

        builder.putToUrl("signature", actualSign);

    }

}
