package exercise02;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

public class ImageWatermarkDetector {
	
	@FXML private ComboBox<String> analysisComboBox;
	@FXML private Slider brightnessSlider;
	@FXML private Slider contrastSlider;
	@FXML private Slider differenceThresholdSlider;
	@FXML private Slider combinedThresholdSlider;
	
	@FXML private Button colorFilterButton;
	@FXML private Button grayButton;
	@FXML private Button edgeButton;
	@FXML private Button smoothButton;
	@FXML private Button sharpButton;
	@FXML private Button fourierButton;
	@FXML private Button prewittEdgeBtn;
	@FXML private Button robertsEdgeBtn;
	@FXML private Button sobelEdgeBtn;
	@FXML private Button inverseButton;
	@FXML private Button neiEdgeButton;
	
	@FXML private ImageView imageView00;
	@FXML private ImageView imageView01;
	@FXML private ImageView imageView10;
	@FXML private ImageView imageView11;
	@FXML private ImageView previewImageView;
	@FXML private ImageView maskImageView;
	@FXML private Button loadImageButton;
	
	@FXML private BarChart<String, Number> histogramChart;
	@FXML private PieChart diffChart;
	@FXML private LineChart<Number, Number> noiseChart;
	
	@FXML private SplitPane splitPane;
	private Image originalImage;
	
	//시계방향 순서로 배치된 imageView 배열
	private ImageView[] imageViews;
	private int colorFilterIndex = 0;  //0: R, 1: G, 2: B
	
	private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	private ScheduledFuture<?> scheduledTask = null;
	
	private ImageView selectedImageView = null;
	
	@FXML
	public void initialize() {
		imageViews = new ImageView[] {imageView00, imageView01, imageView11, imageView10};
		
		for(ImageView iv : imageViews) {
			iv.setPreserveRatio(true);
			iv.setFitWidth(400);
			iv.setFitHeight(400);
		}
		// 각 ImageView 클릭시 테두리 선택 효과 적용
		setupImageViewBorderSelection(imageView00);
		setupImageViewBorderSelection(imageView01);
		setupImageViewBorderSelection(imageView10);
		setupImageViewBorderSelection(imageView11);
		
		// 각 버튼 별 필터 액션 등록
		colorFilterButton.setOnAction(event -> {
			applyColorFilter(colorFilterIndex);
			colorFilterIndex = (colorFilterIndex + 1) % 3;
		});
		
		grayButton.setOnAction(event -> {
			if(originalImage == null) return;
			WritableImage grayImage = applyGrayscaleFilter(originalImage);
			updateImageViews(grayImage);
		});
		
		edgeButton.setOnAction(event -> {
			if(originalImage == null) return;
			
			double[][] edgeKernel = {
					{-1, -1, -1},
					{-1, 8, -1},
					{-1, -1, -1}
			};
			
			WritableImage edgeImage = applyConvolutionFilter(originalImage, edgeKernel);
			updateImageViews(edgeImage);
		});
		
		prewittEdgeBtn.setOnAction(event -> {
			if(originalImage == null) return;
			
			int[][] edgeX = {
					{ -1, 0, 1 },
					{ -1, 0, 1 },
					{ -1, 0, 1 }
			};
			int[][] edgeY = {
					{ -1, -1, -1 },
					{ 0, 0, 0 },
					{ 1, 1, 1 }
			};
			WritableImage prewittEdgeImage = applyEdgeFilter(originalImage, edgeX, edgeY);
			updateImageViews(prewittEdgeImage);
		});
		
		sobelEdgeBtn.setOnAction(event -> {
			if(originalImage == null) return;
			
			int[][] edgeX = {
					{ -1, 0, 1 },
					{ -2, 0, 2 },
					{ -1, 0, 1 }
			};
			int[][] edgeY = {
					{ -1, -2, -1 },
					{ 0, 0, 0 },
					{ 1, 2, 1 }
			};
			WritableImage sobelEdgeImage = applyEdgeFilter(originalImage, edgeX, edgeY);
			updateImageViews(sobelEdgeImage);
		});
		
		robertsEdgeBtn.setOnAction(event -> {
			if(originalImage == null) return;
			
			WritableImage robertsEdgeImage = applyRobertsEdge(originalImage);
			updateImageViews(robertsEdgeImage);
		});
		
		neiEdgeButton.setOnAction(event -> {
			if(originalImage == null) return;
			
			WritableImage neiEdgeImage = applyNeiEdge(originalImage);
			updateImageViews(neiEdgeImage);
		});
		
		inverseButton.setOnAction(event -> {
			if(originalImage == null) return;
			
			WritableImage inverseImage = applyInverse(originalImage);
			updateImageViews(inverseImage);
		});
		
		smoothButton.setOnAction(event -> {
			if(originalImage == null) return;
			
			double[][] smoothKernel = {
				{1.0/9, 1.0/9, 1.0/9},
				{1.0/9, 1.0/9, 1.0/9},
				{1.0/9, 1.0/9, 1.0/9}
			};
			
			WritableImage smoothImage = applyConvolutionFilter(originalImage, smoothKernel);
			updateImageViews(smoothImage);
		});
		
		sharpButton.setOnAction(event -> {
			if(originalImage == null) return;
			
			double[][] sharpKernel = {
				{0, -1, 0},
				{-1, 5, -1},
				{0, -1, 0}
			};
			
			WritableImage sharpImage = applyConvolutionFilter(originalImage, sharpKernel);
			updateImageViews(sharpImage);
		});
		
		fourierButton.setOnAction(event -> {
			applyFourierTransform();
		});
		
		brightnessSlider.setMin(-1.0);
		brightnessSlider.setMax(1.0);
		brightnessSlider.setValue(0.0);
		brightnessSlider.setBlockIncrement(0.1);
		
		brightnessSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
			if(scheduledTask != null) {
				scheduledTask.cancel(false);
			}
			scheduledTask = scheduler.schedule(()-> {
				Platform.runLater(() -> applyBrightnessAdjustment(newVal.doubleValue()));
			}, 200, TimeUnit.MILLISECONDS);  //200ms 디바운스
		});
		
		contrastSlider.setMin(0.0);
		contrastSlider.setMax(2.0);
		contrastSlider.setValue(1.0);
		contrastSlider.setBlockIncrement(0.1);
		
		contrastSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
			applyContrastAdjustment(newVal.doubleValue());
		});
		
		differenceThresholdSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
			if(originalImage != null && imageView00.getImage() != null) {
	            applyImageDifference(newVal.doubleValue());
	        }
		});
		differenceThresholdSlider.setValue(0.1);
		
		combinedThresholdSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
	        double threshold = newVal.doubleValue();
	        applyDifferenceAndEdgeCombined(threshold);
	    });
		applyDifferenceAndEdgeCombined(combinedThresholdSlider.getValue());
		
		analysisComboBox.getItems().addAll("히스토그램 분석", "이미지 차분", "노이즈 패턴 감지");
		analysisComboBox.setOnAction(event -> onAnalysisSelected());
	}
	
	// 선택된 ImageView에 테두리 스타일 토글 적용
	private void setupImageViewBorderSelection(ImageView iv) {
		iv.setOnMouseClicked(event -> {
			System.out.println("Clicked: " + iv);
			if(selectedImageView != null) {
				selectedImageView.getStyleClass().remove("selected-border");
			}
			selectedImageView = iv;
			if(!selectedImageView.getStyleClass().contains("selected-border")) {
				selectedImageView.getStyleClass().add("selected-border");
			}
		});
	}
	
	private void updateFilteredImage(Image filteredImage) {
		//한 칸씩 뒤로 밀기 (시계방향)
		for(int i=imageViews.length - 1; i>0; i--) {
			imageViews[i].setImage(imageViews[i-1].getImage());
		}
		//가장 앞에 새 이미지 넣기
		imageViews[0].setImage(filteredImage);
	}
	
	// 이미지 뷰 업데이트
	private void updateImageViews(Image newImage) {
		//시계방향으로 이미지 한 칸씩 밀기
		imageView10.setImage(imageView11.getImage());
		imageView11.setImage(imageView01.getImage());
		imageView01.setImage(imageView00.getImage());
		imageView00.setImage(newImage);
	}
	
	// 이미지 로드 
	@FXML
	private void onLoadImage() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("이미지 선택");
		fileChooser.getExtensionFilters().addAll(
			new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp")
		);
		File file = fileChooser.showOpenDialog(null);
		if(file != null) {
			try {
				Image loadedImage = new Image(file.toURI().toString());
				if(loadedImage.isError()) {
					showErrorDialog("이미지 로드 실패");
					return;
				}
				originalImage = loadedImage;
				previewImageView.setImage(originalImage);
				updateImageViews(originalImage);
			}catch (Exception e) {
				showErrorDialog("이미지 로드 중 오류 발생: " + e.getMessage());
			}
		}else {
			showErrorDialog("파일 열기 실패");
		}
	}
	
	private void showErrorDialog(String message) {
		Platform.runLater(() -> {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("오류");
			alert.setHeaderText(message);
			alert.setContentText(message);
			alert.showAndWait();
		});
	}
	
	@FXML
	private void onAnalysisSelected() {
		String selected = analysisComboBox.getValue();
		if(selected == null) return;
		
		switch (selected) {
			case "히스토그램 분석":
				applyHistogramAnalysis();
				break;
			case "이미지 차분":
				applyImageDifference(0.1);
				break;
			case "노이즈 패턴 감지":
				applyNoisePatternDetection();
				break;
		}
	}
	
	public void applyColorFilter(int index) {
		if(originalImage == null) return;
		
		WritableImage filteredImage = null;
		
		switch(index) {
			case 0: //빨강 필터
				filteredImage = applyColorChannel(originalImage, "red");
				break;
			case 1:  //초록 필터
				filteredImage = applyColorChannel(originalImage, "green");
				break;
			case 2:  //파랑 필터
				filteredImage = applyColorChannel(originalImage, "blue");
				break;
		}
		
		if(filteredImage != null) {
			updateFilteredImage(filteredImage);
		}
	}
	
	private WritableImage applyColorChannel(Image image, String channel) {
		int width = (int) image.getWidth();
		int height = (int) image.getHeight();
		PixelReader reader = image.getPixelReader();
		WritableImage result = new WritableImage(width, height);
		PixelWriter writer = result.getPixelWriter();
		
		for(int y=0; y<height; y++) {
			for(int x=0; x<width; x++) {
				Color color = reader.getColor(x, y);
				double r = (channel.equalsIgnoreCase("red")) ? color.getRed() : 0;
				double g = (channel.equalsIgnoreCase("green")) ? color.getGreen() : 0;
				double b = (channel.equalsIgnoreCase("blue")) ? color.getBlue() : 0;
				writer.setColor(x, y, new Color(r, g, b, color.getOpacity()));
			}
		}
		return result;
	}
	
	private WritableImage applyInverse(Image image) {
		int width = (int) image.getWidth();
		int height = (int) image.getHeight();
		PixelReader reader = image.getPixelReader();
		WritableImage result = new WritableImage(width, height);
		PixelWriter writer = result.getPixelWriter();
		
		for (int y = 1; y < height - 1; y++) {
		    for (int x = 1; x < width - 1; x++) {
		        Color center = reader.getColor(x, y);
		        Color left = reader.getColor(x - 1, y);
		        Color right = reader.getColor(x + 1, y);
		        Color up = reader.getColor(x, y - 1);
		        Color down = reader.getColor(x, y + 1);

		        // 샤프닝 계산: center * 5 - (left + right + up + down)
		        double r = clamp(center.getRed() * 5 - left.getRed() - right.getRed() - up.getRed() - down.getRed());
		        double g = clamp(center.getGreen() * 5 - left.getGreen() - right.getGreen() - up.getGreen() - down.getGreen());
		        double b = clamp(center.getBlue() * 5 - left.getBlue() - right.getBlue() - up.getBlue() - down.getBlue());

		        // 색상 반전 (inverse)
		        r = clamp(1.0 - r);
		        g = clamp(1.0 - g);
		        b = clamp(1.0 - b);

		        Color inverseColor = new Color(r, g, b, 1.0);
		        writer.setColor(x, y, inverseColor);
		    }
		}
		return result;
	}
	
	private WritableImage applyGrayscaleFilter(Image image) {
		int width = (int) image.getWidth();
		int height = (int) image.getHeight();
		PixelReader reader = image.getPixelReader();
		WritableImage result = new WritableImage(width, height);
		PixelWriter writer = result.getPixelWriter();
		
		for(int y=0; y<height; y++) {
			for(int x=0; x<width; x++) {
				Color color = reader.getColor(x, y);
				double gray = (color.getRed() + color.getGreen() + color.getBlue()) / 3.0;
				writer.setColor(x, y, new Color(gray, gray, gray, color.getOpacity()));
			}
		}
		return result;
	}
	
	private WritableImage applyConvolutionFilter(Image image, double[][] kernel) {
		int width = (int) image.getWidth();
		int height = (int) image.getHeight();
		int kernelSize = kernel.length;
		int offset = kernelSize / 2;
		
		PixelReader reader = image.getPixelReader();
		WritableImage result = new WritableImage(width, height);
		PixelWriter writer = result.getPixelWriter();
		
		for(int y=offset; y<height - offset; y++) {
			for(int x=offset; x<width - offset; x++) {
				double r = 0, g = 0, b = 0;
				
				for(int ky=0; ky < kernelSize; ky++) {
					for(int kx=0; kx < kernelSize; kx++) {
						int px = x + kx - offset;
						int py = y + ky - offset;
						
						Color color = reader.getColor(px, py);
						double weight = kernel[ky][kx];
						
						r += color.getRed() * weight;
						g += color.getGreen() * weight;
						b += color.getBlue() * weight;
					}
				}
				
				//클램핑: 0~1 사이로 조정
				r = Math.min(Math.max(r, 0), 1);
				g = Math.min(Math.max(g, 0), 1);
				b = Math.min(Math.max(b, 0), 1);
				
				writer.setColor(x, y, new Color(r, g, b, 1.0));
			}
		}
		return result;
	}
	
	private WritableImage applyEdgeFilter(Image image, int[][] edgeX, int[][] edgeY) {
		
		int width = (int) image.getWidth();
		int height = (int) image.getHeight();
		
		PixelReader reader = image.getPixelReader();
		WritableImage result = new WritableImage(width, height);
		PixelWriter writer = result.getPixelWriter();
		
		for(int y=1; y<height - 1; y++) {
			for(int x=1; x<width - 1; x++) {
				double gx = 0.0;
				double gy = 0.0;
				
				for(int j=-1; j<=1; j++) {
					for(int i=-1; i<=1; i++) {
						Color color = reader.getColor(x + i, y + j);
						double gray = (color.getRed() + color.getGreen() + color.getBlue()) / 3.0;
						
						gx += edgeX[j + 1][i + 1] * gray;
						gy += edgeY[j + 1][i + 1] * gray;
					}
				}
				double magnitudeRaw = Math.sqrt(gx * gx + gy * gy);
				double magnitude = Math.min(1.0, Math.max(0.0, magnitudeRaw));
				Color edgeColor = new Color(magnitude, magnitude, magnitude, 1.0);
				writer.setColor(x, y, edgeColor);
			}
		}
		return result;
	}
	
	private WritableImage applyNeiEdge(Image image) {
		int width = (int) image.getWidth();
		int height = (int) image.getHeight();
		PixelReader reader = image.getPixelReader();
		WritableImage result = new WritableImage(width, height);
		PixelWriter writer = result.getPixelWriter();
		
		int edge = 5;  // 엣지 민감도 값
		
		for (int y = 0; y < height; y++) {
		    for (int x = 0; x < width; x++) {
		        Color c = reader.getColor(x, y);
		        double oneColor = (c.getRed() + c.getGreen() + c.getBlue()) / 3.0;
		        
		        double oneRColor = oneColor; // 기본값: 현재 픽셀 값
		        double oneUColor = oneColor;
		        
		        if (x + 1 < width) {
		            Color Rc = reader.getColor(x + 1, y);
		            oneRColor = (Rc.getRed() + Rc.getGreen() + Rc.getBlue()) / 3.0;
		        }
		        if (y + 1 < height) {
		            Color Uc = reader.getColor(x, y + 1);
		            oneUColor = (Uc.getRed() + Uc.getGreen() + Uc.getBlue()) / 3.0;
		        }
		        
		        // 밝기 차이 임계값 체크 (0~1 범위이므로 edge 값도 0~1로 조정 필요)
		        double threshold = edge / 255.0;
		        
		        Color edgeColor;
		        if (Math.abs(oneColor - oneRColor) > threshold || Math.abs(oneColor - oneUColor) > threshold) {
		            edgeColor = new Color(0, 0, 0, 1.0);  // 검정 (엣지)
		        } else {
		            edgeColor = new Color(1, 1, 1, 1.0);  // 흰색 (배경)
		        }
		        
		        writer.setColor(x, y, edgeColor);
		    }
		}
		return result;
	}
	
	private WritableImage applyRobertsEdge(Image image) {
		
		int width = (int) image.getWidth();
		int height = (int) image.getHeight();
		
		PixelReader reader = image.getPixelReader();
		WritableImage result = new WritableImage(width, height);
		PixelWriter writer = result.getPixelWriter();
		
		for (int y = 1; y < height - 1; y++) {
		    for (int x = 1; x < width - 1; x++) {
		        Color c1 = reader.getColor(x, y);
		        Color c2 = reader.getColor(x + 1, y + 1);
		        Color c3 = reader.getColor(x + 1, y);
		        Color c4 = reader.getColor(x, y + 1);

		        double gray1 = (c1.getRed() + c1.getGreen() + c1.getBlue()) / 3.0;
		        double gray2 = (c2.getRed() + c2.getGreen() + c2.getBlue()) / 3.0;
		        double gray3 = (c3.getRed() + c3.getGreen() + c3.getBlue()) / 3.0;
		        double gray4 = (c4.getRed() + c4.getGreen() + c4.getBlue()) / 3.0;

		        double gx = gray1 - gray2;
		        double gy = gray3 - gray4;

		        double magnitude = Math.min(1.0, Math.sqrt(gx * gx + gy * gy)); // 0~1 클램핑

		        Color edgeColor = new Color(magnitude, magnitude, magnitude, 1.0);
		        writer.setColor(x, y, edgeColor);
		    }
		}
		return result;
	}
	
	private void applyFourierTransform() {
		System.out.println("applyFourierTransform called");
		if(originalImage == null) return;
		
		Task<Void> task = new Task<Void>() {
			
			@Override
			protected Void call() throws Exception {
				Image resizedImage = resizeToPowerOfTwo(originalImage);
				int width = (int) resizedImage.getWidth();
				int height = (int) resizedImage.getHeight();
				PixelReader reader = resizedImage.getPixelReader();
				
				//그레이스케일 변환
				double[][] gray = new double[height][width];
				for(int y=0; y<height; y++) {
					for(int x=0; x<width; x++) {
						Color color = reader.getColor(x, y);
						double luminance = (color.getRed() + color.getGreen() + color.getBlue()) / 3.0;
						gray[y][x] = luminance;
					}
				}
				//복소수 배열 초기화
				Complex[][] input = new Complex[height][width];
				for(int y=0; y<height; y++) {
					for(int x=0; x<width; x++) {
						input[y][x] = new Complex(gray[y][x], 0);
					}
				}
				
				//2D FFT 수행
				Complex[][] fftResult = FFT2D.fft2D(input);
				
				//스펙트럼 시각화
				WritableImage spectrumImage = createSpectrumImage(fftResult);
				Platform.runLater(() -> {
					updateImageViews(spectrumImage);
					System.out.println("스펙트럼 이미지 업데이트 완료");
				});
				return null;
			}
		};
		new Thread(task).start();
	}
	
	private WritableImage createSpectrumImage(Complex[][] fftData) {
		int height = fftData.length;
		int width = fftData[0].length;
		WritableImage result = new WritableImage(width, height);
		PixelWriter writer = result.getPixelWriter();
		
		//로그 스케일과 중심 이동
		double maxLog = Double.NEGATIVE_INFINITY;
		double[][] logMagnitude = new double[height][width];
		
		for(int y=0; y<height; y++) {
			for(int x=0; x<width; x++) {
				double mag = fftData[y][x].abs();
				double logVal = Math.log(1 + mag);
				logMagnitude[y][x] = logVal;
				if(logVal > maxLog) maxLog = logVal;
			}
		}
		
		//중심 이동 & 픽셀 쓰기
		for(int y=0; y<height; y++) {
			for(int x=0; x<width; x++) {
				int shiftedX = (x + width / 2) % width;
				if(shiftedX < 0) shiftedX += width;
				
				int shiftedY = (y + height / 2) % height;
				if(shiftedY < 0) shiftedY += height;
				
				double norm = logMagnitude[shiftedY][shiftedX] / maxLog;
				Color gray = new Color(norm, norm, norm, 1.0);
				writer.setColor(x, y, gray);
			}
		}
		return result;
	}
	
	private void applyBrightnessAdjustment(double offset) {
		if(originalImage == null) return;
		
		int width = (int) originalImage.getWidth();
		int height = (int) originalImage.getHeight();
		PixelReader reader = originalImage.getPixelReader();
		WritableImage result = new WritableImage(width, height);
		PixelWriter writer = result.getPixelWriter();
		
		for(int y=0; y<height; y++) {
			for(int x=0; x<width; x++) {
				Color color = reader.getColor(x, y);
				
				double r = clamp(color.getRed() + offset);
				double g = clamp(color.getGreen() + offset);
				double b = clamp(color.getBlue() + offset);
				
				writer.setColor(x, y, new Color(r, g, b, color.getOpacity()));
			}
		}
		updateImageViews(result);
	}
	
	private double clamp(double value) {
		return Math.min(1.0, Math.max(0.0, value));
	}
	
	private void applyContrastAdjustment(double factor) {
		if(originalImage == null) return;
		
		int width = (int) originalImage.getWidth();
		int height= (int) originalImage.getHeight();
		PixelReader reader = originalImage.getPixelReader();
		WritableImage result = new WritableImage(width, height);
		PixelWriter writer = result.getPixelWriter();
		
		for(int y=0; y<height; y++) {
			for(int x=0; x<width; x++) {
				Color color = reader.getColor(x, y);
				
				//중간값(0.5)을 기준으로 명암 대비 조정
				double r = clamp((color.getRed() - 0.5) * factor + 0.5);
				double g = clamp((color.getGreen() - 0.5) * factor + 0.5);
				double b = clamp((color.getBlue() - 0.5) * factor + 0.5);
				
				writer.setColor(x, y, new Color(r, g, b, color.getOpacity()));
			}
		}
		updateImageViews(result);
	}
	
	private void applyHistogramAnalysis() {
		if(originalImage == null) return;
		
		int width = (int) originalImage.getWidth();
		int height= (int) originalImage.getHeight();
		PixelReader reader = originalImage.getPixelReader();
		
		int[] redHist = new int[256];
		int[] greenHist = new int[256];
		int[] blueHist = new int[256];
		
		for(int y=0; y<height; y++) {
			for(int x=0; x<width; x++) {
				Color color = reader.getColor(x, y);
				int rIndex = Math.min(255, Math.max(0, (int)(color.getRed() * 255)));
				redHist[rIndex]++;
				int gIndex = Math.min(255, Math.max(0, (int)(color.getGreen() * 255)));
				greenHist[gIndex]++;
				int bIndex = Math.min(255, Math.max(0, (int)(color.getBlue() * 255)));
				blueHist[bIndex]++;
			}
		}
		XYChart.Series<String, Number> redSeries = new XYChart.Series<>();
		redSeries.setName("Red");
		
		XYChart.Series<String, Number> greenSeries = new XYChart.Series<>();
		greenSeries.setName("Green");
		
		XYChart.Series<String, Number> blueSeries = new XYChart.Series<>();
		blueSeries.setName("Blue");
		
		for(int i=0; i<256; i++) {
			redSeries.getData().add(new XYChart.Data<>(String.valueOf(i), redHist[i]));
			greenSeries.getData().add(new XYChart.Data<>(String.valueOf(i), greenHist[i]));
			blueSeries.getData().add(new XYChart.Data<>(String.valueOf(i), blueHist[i]));
		}
		
		CategoryAxis xAxis = (CategoryAxis) histogramChart.getXAxis();
		NumberAxis yAxis = (NumberAxis) histogramChart.getYAxis();
		
		xAxis.setTickLabelFill(Color.web("#84FFFF"));
		yAxis.setTickLabelFill(Color.web("#84FFFF"));

		changeAxisLabelColor(xAxis, Color.web("#84FFFF"));
		changeAxisLabelColor(yAxis, Color.web("#84FFFF"));
		
		histogramChart.getData().clear();
		histogramChart.getData().addAll(redSeries, greenSeries, blueSeries);
		
		Platform.runLater(() -> {
			
	        histogramChart.lookupAll(".default-color0.chart-bar").forEach(node -> {
	            node.setStyle("-fx-bar-fill: #00264B;");
	        });
	        histogramChart.lookupAll(".default-color1.chart-bar").forEach(node -> {
	            node.setStyle("-fx-bar-fill: #00FF00;");
	        });
	        histogramChart.lookupAll(".default-color2.chart-bar").forEach(node -> {
	            node.setStyle("-fx-bar-fill: #4682B4;");
	        });
	    });
	}
	
	private void changeAxisLabelColor(Axis<?> axis, Color color) {
	    for (Node node : axis.lookupAll(".axis-label")) {
	        if (node instanceof Label) {
	            ((Label) node).setTextFill(color);
	        }
	    }
	}
	
	private void applyImageDifference(double threshold) {
		System.out.println("applyImageDifference called");
		if(originalImage == null || imageView00.getImage() == null) {
			System.out.println("원본 또는 필터 이미지 없음");
			return;
		}
		
		Image filtered = imageView00.getImage();
		int width = (int) originalImage.getWidth();
		int height= (int) originalImage.getHeight();
		
		PixelReader readerOriginal = originalImage.getPixelReader();
		PixelReader readerFiltered = filtered.getPixelReader();
		
		int totalPixels = width * height;
		int changedPixels = 0;
		
		WritableImage maskImage = new WritableImage(width, height);
		PixelWriter maskWriter = maskImage.getPixelWriter();
		
		for(int y=0; y<height; y++) {
			for(int x=0; x<width; x++) {
				Color c1 = readerOriginal.getColor(x, y);
				Color c2 = readerFiltered.getColor(x, y);
				
				double diff = Math.abs(c1.getRed() - c2.getRed())
							+ Math.abs(c1.getGreen() - c2.getGreen())
							+ Math.abs(c1.getBlue() - c2.getBlue());
				
				if((diff / 3.0) > threshold) {
					changedPixels++;
					maskWriter.setColor(x, y, Color.GREEN);
				}else {
					maskWriter.setColor(x, y, Color.TRANSPARENT);
				}
			}
		}
		int unchangedPixels = totalPixels - changedPixels;
		double changedPercent = (double) changedPixels / totalPixels * 100;
		double unchangedPercent = 100.0 - changedPercent;
		
		ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
			new PieChart.Data("변화됨(" + String.format("%.1f", changedPercent) + "%)", changedPixels),
			new PieChart.Data("유지됨(" + String.format("%.1f", unchangedPercent) + "%)", unchangedPixels)
		);
		
		Platform.runLater(() -> {
		    diffChart.setData(pieData);
		    diffChart.setTitle("원본과 필터 이미지 차이 분석");
		    diffChart.layout();

		    // 파이차트 조각 스타일 직접 변경
		    int i = 0;
		    for (PieChart.Data data : diffChart.getData()) {
		        Node node = data.getNode();
		        if (node != null) {
		            String color = (i == 0) ? "#84FFFF" : "#00264B";  // 시리즈별 색상 지정
		            node.setStyle("-fx-pie-color: " + color + ";");
		        }
		        i++;
		    }
		    
		    if(maskImageView != null) {
		    	maskImageView.setImage(maskImage);
		    }else {
		    	System.out.println("maskImageView가 null입니다. UI에 ImageView를 추가하고 @FXML 연결을 확인하세요.");
		    }
		    
		    System.out.println("파이차트 및 마스크 이미지 업데이트 완료");
		});
	}
	
	private void applyDifferenceAndEdgeCombined(double threshold) {
	    if(originalImage == null || imageView00.getImage() == null) {
	        System.out.println("원본 또는 필터 이미지 없음");
	        return;
	    }

	    Image filtered = imageView00.getImage();
	    int width = (int) originalImage.getWidth();
	    int height = (int) originalImage.getHeight();

	    PixelReader readerOriginal = originalImage.getPixelReader();
	    PixelReader readerFiltered = filtered.getPixelReader();

	    // 1) 엣지 필터 적용 (Sobel 예시)
	    WritableImage edgeImage = applyEdgeFilter(filtered,
	        new int[][]{{ -1, 0, 1 },{ -2, 0, 2 },{ -1, 0, 1 }},
	        new int[][]{{ -1, -2, -1 },{ 0, 0, 0 },{ 1, 2, 1 }});
	    PixelReader edgeReader = edgeImage.getPixelReader();

	    WritableImage combinedImage = new WritableImage(width, height);
	    PixelWriter writer = combinedImage.getPixelWriter();

	    for(int y=0; y<height; y++) {
	        for(int x=0; x<width; x++) {
	            Color c1 = readerOriginal.getColor(x, y);
	            Color c2 = readerFiltered.getColor(x, y);

	            double diff = Math.abs(c1.getRed() - c2.getRed())
	                        + Math.abs(c1.getGreen() - c2.getGreen())
	                        + Math.abs(c1.getBlue() - c2.getBlue());

	            boolean diffFlag = (diff / 3.0) > threshold;

	            Color edgeColor = edgeReader.getColor(x, y);
	            double edgeGray = (edgeColor.getRed() + edgeColor.getGreen() + edgeColor.getBlue()) / 3.0;
	            boolean edgeFlag = edgeGray > 0.2;  // 엣지 임계값 조정 가능

	            if(diffFlag || edgeFlag) {
	                writer.setColor(x, y, Color.RED);
	            } else {
	                writer.setColor(x, y, Color.TRANSPARENT);
	            }
	        }
	    }
	    
	    // 결과를 마스크 이미지뷰에 표시
	    maskImageView.setImage(combinedImage);
	}
	
	private void applyNoisePatternDetection() {
		if(originalImage == null) return;
		
		Image resizedImage = resizeToPowerOfTwo(originalImage);
		int width = (int) resizedImage.getWidth();
		int height= (int) resizedImage.getHeight();
		PixelReader reader = resizedImage.getPixelReader();
		
		// 그레이스케일 변환
		double[][] gray = new double[height][width];
		for(int y=0; y<height; y++) {
			for(int x=0; x<width; x++) {
				Color color = reader.getColor(x, y);
				gray[y][x] = (color.getRed() + color.getGreen() + color.getBlue()) / 3.0;
			}
		}
		
		// 복소수 배열로 변환
		Complex[][] input = new Complex[height][width];
		for(int y=0; y<height; y++) {
			for(int x=0; x<width; x++) {
				input[y][x] = new Complex(gray[y][x], 0);
			}
		}
		
		// 2D 푸리에 변환
		Complex[][] fftResult = FFT2D.fft2D(input);
		
		// 수평 방향 평균 주파수 세기 계산 (줄 단위로 합산)
		double[] spectrumX = new double[width];
		for(int x=0; x<width; x++) {
			double sum = 0;
			for(int y=0; y<height; y++) {
				sum += fftResult[y][x].abs();
			}
			spectrumX[x] = sum / height;
		}
		
		// 수직 방향 평균 주파수 세기 추가
	    double[] spectrumY = new double[height];
	    for(int y=0; y<height; y++) {
	        double sum = 0;
	        for(int x=0; x<width; x++) {
	            sum += fftResult[y][x].abs();
	        }
	        spectrumY[y] = sum / width;
	    }
		
		// LineChart에 표시
		int stepX = Math.max(1, width / 128);
		int stepY = Math.max(1, height / 128);
		
		XYChart.Series<Number, Number> seriesX = new XYChart.Series<>();
		seriesX.setName("수평 방향 주파수 세기");
		
		XYChart.Series<Number, Number> seriesY = new XYChart.Series<>();
		seriesY.setName("수직 방향 주파수 세기");
		
		for(int x=0; x<width; x += stepX) {
			seriesX.getData().add(new XYChart.Data<>(x, spectrumX[x]));
		}
		
		for(int y=0; y<height; y += stepY) {
			seriesY.getData().add(new XYChart.Data<>(y, spectrumY[y]));
		}
		
		noiseChart.getData().clear();
		noiseChart.getData().addAll(seriesX, seriesY);
		noiseChart.setTitle("노이즈 패턴 주파수 스펙트럼 (수평+수직)");
		
		// 축 객체 직접 가져오기
	    Axis<?> xAxis = noiseChart.getXAxis();
	    Axis<?> yAxis = noiseChart.getYAxis();
	    
	    // 눈금 숫자 색 변경
	    xAxis.setTickLabelFill(Color.web("#84FFFF"));
	    yAxis.setTickLabelFill(Color.web("#84FFFF"));
	    
	    // 축 제목 색 변경 (직접 lookupAll 해서 Label 찾기)
	    changeAxisLabelColor(xAxis, Color.web("#84FFFF"));
	    changeAxisLabelColor(yAxis, Color.web("#84FFFF"));
	    
	    noiseChart.getStylesheets().clear();
	    noiseChart.getStylesheets().add(getClass().getResource("chart-style.css").toExternalForm());
	}
	
	public Image resizeToPowerOfTwo(Image image) {
		int originalWidth = (int) image.getWidth();
		int originalHeight = (int) image.getHeight();

	    int newWidth = nextPowerOfTwo(originalWidth);
	    int newHeight = nextPowerOfTwo(originalHeight);

	    WritableImage resizedImage = new WritableImage(newWidth, newHeight);
	    PixelReader reader = image.getPixelReader();
	    PixelWriter writer = resizedImage.getPixelWriter();

	    for (int y = 0; y < newHeight; y++) {
	        for (int x = 0; x < newWidth; x++) {
	            if (x < originalWidth && y < originalHeight) {
	                writer.setArgb(x, y, reader.getArgb(x, y));
	            } else {
	                writer.setColor(x, y, Color.BLACK); // 남는 부분은 검정색
	            }
	        }
	    }

	    return resizedImage;
	}

	private int nextPowerOfTwo(int n) {
	    int power = 1;
	    while (power < n) {
	        power *= 2;
	    }
	    return power;
	}
}
