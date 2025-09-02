package com.chrisimoni.evyntspace.notification.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;

public class NotificationUtil {
    public static String generateQrCodeDataUrl(String payload) {
        if (payload == null || payload.isBlank()) {
            throw new IllegalArgumentException("QR payload must not be empty.");
        }

        int size = 300; // px (square)
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
        hints.put(EncodeHintType.MARGIN, 1); // quiet zone
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M); // L/M/Q/H

        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(payload, BarcodeFormat.QR_CODE, size, size, hints);

            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
            return "data:image/png;base64," + base64;
        } catch (WriterException | java.io.IOException e) {
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

    public static String createMapLink(String fullAddress) {
        try {
            String encoded = URLEncoder.encode(fullAddress, StandardCharsets.UTF_8.toString());
            return "https://www.google.com/maps/search/?api=1&query=" + encoded;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to encode address for map link", e);
        }
    }
}
