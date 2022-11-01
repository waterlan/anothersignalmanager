package math;

public class FFT {

    public FFT() {
        // TODO Auto-generated constructor stub
    }

    /**
     * Perform a one dimensional FFT in place.
     * 
     * @param real
     * @param imag
     * @param npoints length of array.
     * @param dir     1: forward fft, -1: inverse fft.
     * @return
     */
    public static int fft(double[] real, double[] imag, int npoints, int dir) {
        double temp;
        double tr, ti, angle, wr, wi;

        /* Swap the input elements for the decimation in time algorithm. */

        for (int index = 1, swapindex = 0; index < npoints; index++) {
            int k = npoints;
            do
                k /= 2;
            while ((swapindex + k) >= npoints);
            swapindex = (swapindex % k) + k;
            if (swapindex <= index)
                continue;
            temp = real[index];
            real[index] = real[swapindex];
            real[swapindex] = temp;
            temp = imag[index];
            imag[index] = imag[swapindex];
            imag[swapindex] = temp;
        }

        /*
         * Do the butterfly computations.
         *
         * stage index: k = 1, 2, 4, 8 . . . For npoints = 8, for example, there will be
         * three stages of butterfly computations. b'fly indices: i and j will be
         * separated by a distance dependent on the current stage. k is used as the
         * separation constant.
         */

        for (int k = 1; k < npoints; k *= 2) {
            for (int index = 0; index < k; index++) {
                angle = Math.PI * ((double) (index * -dir)) / ((double) k);
                wr = Math.cos(angle);
                wi = Math.sin(angle);
                for (int i = index; i < npoints; i += 2 * k) {
                    int j = i + k;
                    tr = (wr * real[j]) - (wi * imag[j]);
                    ti = (wr * imag[j]) + (wi * real[j]);

                    real[j] = real[i] - tr;
                    imag[j] = imag[i] - ti;
                    real[i] += tr;
                    imag[i] += ti;
                }
            }
        }

        wr = Math.sqrt(1.0 / (double) npoints);

        /* scale the output by sqrt(1./N) */

        for (int i = 0; i < npoints; i++) {
            real[i] *= wr;
            imag[i] *= wr;
        }
        return 0;
    }

}
