package exercise02;

public class FFT {

    // 1D 푸리에 변환 (입력: 복소수 배열, 출력: 푸리에 변환된 복소수 배열)
    public static Complex[] fft(Complex[] x) {
        int n = x.length;

        if (n == 1) return new Complex[]{x[0]};

        if ((n & (n - 1)) != 0) {
            throw new IllegalArgumentException("길이는 2의 거듭제곱이어야 합니다.");
        }

        Complex[] even = new Complex[n / 2];
        Complex[] odd = new Complex[n / 2];

        for (int k = 0; k < n / 2; k++) {
            even[k] = x[2 * k];
            odd[k] = x[2 * k + 1];
        }

        Complex[] q = fft(even);
        Complex[] r = fft(odd);

        Complex[] y = new Complex[n];
        for (int k = 0; k < n / 2; k++) {
            double kth = -2 * Math.PI * k / n;
            Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
            y[k] = q[k].plus(wk.times(r[k]));
            y[k + n / 2] = q[k].minus(wk.times(r[k]));
        }
        return y;
    }

    // 1D 역푸리에 변환
    public static Complex[] ifft(Complex[] x) {
        int n = x.length;
        Complex[] y = new Complex[n];

        for (int i = 0; i < n; i++) {
            y[i] = x[i].conjugate();
        }

        y = fft(y);

        for (int i = 0; i < n; i++) {
            y[i] = y[i].conjugate().scale(1.0 / n);
        }

        return y;
    }
}