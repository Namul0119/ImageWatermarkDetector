package exercise02;

public class FFT2D {

    public static Complex[][] fft2D(Complex[][] input) {
        int height = input.length;
        int width = input[0].length;
        Complex[][] temp = new Complex[height][width];
        Complex[][] output = new Complex[height][width];

        // 행 방향 FFT
        for (int y = 0; y < height; y++) {
            temp[y] = FFT.fft(input[y]);
        }

        // 열 방향 FFT
        for (int x = 0; x < width; x++) {
            Complex[] column = new Complex[height];
            for (int y = 0; y < height; y++) {
                column[y] = temp[y][x];
            }
            Complex[] fftCol = FFT.fft(column);
            for (int y = 0; y < height; y++) {
                output[y][x] = fftCol[y];
            }
        }

        return output;
    }
}