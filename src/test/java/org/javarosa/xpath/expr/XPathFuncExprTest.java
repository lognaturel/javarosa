package org.javarosa.xpath.expr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.javarosa.test.utils.SystemHelper.withTimeZone;
import static org.javarosa.xpath.expr.XPathFuncExpr.toDate;
import static org.javarosa.xpath.expr.XPathFuncExpr.toDouble;

import java.time.Instant;
import java.util.Date;
import java.util.TimeZone;
import org.junit.Test;

public class XPathFuncExprTest {

    @Test
    public void isIdempotent_whenArgsContainsNonIdempotentFunc_returnsFalse() {
        // string(random())
        XPathFuncExpr expr = new XPathFuncExpr(new XPathQName("string"), new XPathExpression[] {
            new XPathFuncExpr(new XPathQName("random"))
        });

        assertThat(expr.isIdempotent(), equalTo(false));
    }

    @Test
    public void containsFunc_whenFunctionNameMatches_returnTrue() {
        // random()
        XPathFuncExpr expr = new XPathFuncExpr(new XPathQName("random"));
        assertThat(expr.containsFunc("random"), equalTo(true));
    }

    @Test
    public void containsFunc_whenArgsIncludeFunction_returnTrue() {
        // string(random())
        XPathFuncExpr expr = new XPathFuncExpr(new XPathQName("string"), new XPathExpression[] {
            new XPathFuncExpr(new XPathQName("random"))
        });

        assertThat(expr.containsFunc("random"), equalTo(true));
    }

    @Test
    public void containFunc_whenFunctionNameDoesNotMatch_returnsFalse() {
        // random()
        XPathFuncExpr expr = new XPathFuncExpr(new XPathQName("random"));
        assertThat(expr.containsFunc("other"), equalTo(false));
    }

    @Test
    public void toDateAndToDouble_areInverses() {
        Date original = Date.from(Instant.parse("2021-11-30T12:34:56Z"));

        withTimeZone(TimeZone.getTimeZone("UTC"), () -> {
            Date result = (Date) toDate(toDouble(original), true);
            assertThat((double) result.getTime(), closeTo((double) original.getTime(), 1.0));
        });

        withTimeZone(TimeZone.getTimeZone("GMT-08:00"), () -> {
            Date result = (Date) toDate(toDouble(original), true);
            assertThat((double) result.getTime(), closeTo((double) original.getTime(), 1.0));
        });

        withTimeZone(TimeZone.getTimeZone("GMT+08:00"), () -> {
            Date result = (Date) toDate(toDouble(original), true);
            assertThat((double) result.getTime(), closeTo((double) original.getTime(), 1.0));
        });
    }

    @Test
    public void toDateAndToDouble_areInverses_acrossDstTransitions() {
        withTimeZone(TimeZone.getTimeZone("America/Los_Angeles"), () -> {
            for (String instant : new String[] {
                "2021-03-13T12:00:00-08:00",
                "2021-03-14T12:00:00-07:00",
                "2021-11-06T12:00:00-07:00",
                "2021-11-07T12:00:00-08:00"
            }) {
                Date original = Date.from(Instant.parse(instant));

                Date result = (Date) toDate(toDouble(original), true);

                assertThat((double) result.getTime(), closeTo((double) original.getTime(), 1.0));
            }
        });
    }
}