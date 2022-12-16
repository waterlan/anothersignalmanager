package signals;

public class ComplexArray {

    final public double[] re;
    final public double[] im;
    final public int length;

    public ComplexArray(int length) {
        this.re = new double[length];
        this.im = new double[length];
        this.length = length;
    }
}
