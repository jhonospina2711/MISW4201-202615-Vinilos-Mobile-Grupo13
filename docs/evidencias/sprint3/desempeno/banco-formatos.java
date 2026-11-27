import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Compara el costo de crear el formateador en cada llamada frente a
 * reutilizarlo. Mide el mismo trabajo que hacían HU06 (precio por álbum) y
 * los detalles (fecha por fila) en el código anterior.
 */
public class BenchFormat {

    static final int N = 10_000;
    static final Locale ES_CO = Locale.forLanguageTag("es-CO");

    interface Op { String run(int i); }

    static void bench(String label, Op op) {
        // calentamiento
        for (int i = 0; i < 2_000; i++) op.run(i);
        long t0 = System.nanoTime();
        long sink = 0;
        for (int i = 0; i < N; i++) sink += op.run(i).length();
        long t1 = System.nanoTime();
        double totalMs = (t1 - t0) / 1_000_000.0;
        System.out.printf("%-46s %8.1f ms total  %7.2f us/op  (sink %d)%n",
                label, totalMs, totalMs * 1000.0 / N, sink);
    }

    public static void main(String[] args) {
        final double price = 1_234_567.89;
        final String raw = "1984-08-01";

        NumberFormat cachedNumber = NumberFormat.getCurrencyInstance(ES_CO);
        final SimpleDateFormat cachedIso = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        final SimpleDateFormat cachedLong =
                new SimpleDateFormat("d 'de' MMMM 'de' yyyy", ES_CO);

        System.out.println("== NumberFormat (precio por álbum, HU06) ==");
        bench("crear por llamada", i -> NumberFormat.getCurrencyInstance(ES_CO).format(price));
        bench("reutilizar", i -> cachedNumber.format(price));

        System.out.println();
        System.out.println("== SimpleDateFormat (fecha por fila, HU02/HU04/HU06) ==");
        bench("crear 2 por llamada + parse + format", i -> {
            try {
                Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(raw);
                return new SimpleDateFormat("d 'de' MMMM 'de' yyyy", ES_CO).format(d);
            } catch (Exception e) { return ""; }
        });
        bench("reutilizar 2 formateadores", i -> {
            try {
                return cachedLong.format(cachedIso.parse(raw));
            } catch (Exception e) { return ""; }
        });
    }
}
