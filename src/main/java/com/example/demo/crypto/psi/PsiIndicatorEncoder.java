
package com.example.demo.crypto.psi;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.entity.ThreatIndicator;

@Service
public class PsiIndicatorEncoder {

    private final PsiHashToCurve hashToCurve;

    public PsiIndicatorEncoder(PsiHashToCurve hashToCurve) {
        this.hashToCurve = hashToCurve;
    }

    /**
     * Converts threat indicators into PSI-compatible curve points.
     *
     * Each encoded indicator retains its local database ID.
     * Raw indicator values are not included in the result.
     */
    public List<EncodedIndicator> encodeIndicators(
            List<ThreatIndicator> indicators) {

        if (indicators == null) {
            throw new IllegalArgumentException(
                    "Threat indicator list cannot be null");
        }

        if (indicators.isEmpty()) {
            return List.of();
        }

        if (indicators.size() > 100_000) {
            throw new IllegalArgumentException(
                    "Too many indicators for one PSI operation");
        }

        List<EncodedIndicator> encodedIndicators =
                new ArrayList<>();

        for (ThreatIndicator indicator : indicators) {

            if (indicator == null) {
                throw new IllegalArgumentException(
                        "Threat indicator cannot be null");
            }

            if (indicator.getId() == null) {
                throw new IllegalArgumentException(
                        "Threat indicator must have a database ID");
            }

            if (indicator.getIndicatorType() == null
                    || indicator.getIndicatorType().isBlank()) {
                throw new IllegalArgumentException(
                        "Indicator type is required");
            }

            if (indicator.getIndicatorValue() == null
                    || indicator.getIndicatorValue().isBlank()) {
                throw new IllegalArgumentException(
                        "Indicator value is required");
            }

            String indicatorType =
                    indicator.getIndicatorType()
                            .trim()
                            .toUpperCase();

            String canonicalValue =
                    indicator.getIndicatorValue().trim();

            byte[] encodedPoint =
                    hashToCurve.hashIndicator(
                            indicatorType,
                            canonicalValue
                    );

            encodedIndicators.add(
                    new EncodedIndicator(
                            indicator.getId(),
                            indicatorType,
                            encodedPoint
                    )
            );
        }

        return List.copyOf(encodedIndicators);
    }

    public record EncodedIndicator(
            Long localIndicatorId,
            String indicatorType,
            byte[] encodedPoint) {

        public EncodedIndicator {
            if (localIndicatorId == null) {
                throw new IllegalArgumentException(
                        "Local indicator ID is required");
            }

            if (indicatorType == null || indicatorType.isBlank()) {
                throw new IllegalArgumentException(
                        "Indicator type is required");
            }

            if (encodedPoint == null || encodedPoint.length == 0) {
                throw new IllegalArgumentException(
                        "Encoded point is required");
            }

            encodedPoint = encodedPoint.clone();
        }

        @Override
        public byte[] encodedPoint() {
            return encodedPoint.clone();
        }
    }
}
