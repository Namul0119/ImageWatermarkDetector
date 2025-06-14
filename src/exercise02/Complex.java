package exercise02;

public class Complex {
    private final double re;   // 실수부
    private final double im;   // 허수부

    public Complex(double real, double imag) {
        this.re = real;
        this.im = imag;
    }

    public double re() { return re; }
    public double im() { return im; }

    public Complex plus(Complex b) {
        return new Complex(this.re + b.re, this.im + b.im);
    }

    public Complex minus(Complex b) {
        return new Complex(this.re - b.re, this.im - b.im);
    }

    public Complex times(Complex b) {
        double real = this.re * b.re - this.im * b.im;
        double imag = this.re * b.im + this.im * b.re;
        return new Complex(real, imag);
    }

    public Complex scale(double alpha) {
        return new Complex(alpha * re, alpha * im);
    }

    public Complex conjugate() {
        return new Complex(re, -im);
    }

    public double abs() {
        return Math.hypot(re, im);
    }

    public Complex exp() {
        return new Complex(Math.exp(re) * Math.cos(im), Math.exp(re) * Math.sin(im));
    }

    public Complex times(double scalar) {
        return new Complex(re * scalar, im * scalar);
    }

    public Complex divide(Complex b) {
        return this.times(b.conjugate()).scale(1.0 / b.abs() / b.abs());
    }

    public String toString() {
        if (im == 0) return re + "";
        if (re == 0) return im + "i";
        if (im < 0) return re + " - " + (-im) + "i";
        return re + " + " + im + "i";
    }
}