package mc.dragons.core.util;

public class MathUtil {
	
	private MathUtil() {}
	
	public static double round(double x, double p) {
		double factor = Math.pow(10, p);
		return Math.round(x * factor) / factor;
	}
	
	public static double round(double x) {
		return round(x, 2);
	}
}
