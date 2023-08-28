package by.parser.parserostrovshop;

import javafx.concurrent.Task;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class ParseTask extends Task<Void> {

    private final String path;
    private final ArrayList<String> categories;

    public ParseTask(String path, ArrayList<String> categories) {
        this.path = path;
        this.categories = categories;
    }

    ArrayList<String> links = new ArrayList<>();


    @Override
    protected Void call() {

        updateMessage("Подключение к серверу...");
        ArrayList<Nomenclature> nomenclatures = new ArrayList<>();

        for (String category : categories) {
            try {
                getLinks(category);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        updateProgress(100, 10000);
        long pathCat = 9000 / links.size();
        long i = pathCat;

        for (String link : links) {
            updateMessage("Получение данных по ссылке " + link);

            String name;
            String brand = null;
            long barcode = 0;
            String type = null;
            String size = null;
            int countInPackage = 0;
            double price;
            double oldPrice = 0.0;

            Document document;
            try {
                document = Jsoup.connect(link)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
                        .timeout(5000)
                        .ignoreContentType(true)
                        .ignoreHttpErrors(true)
                        .referrer("https://www.google.com/")
                        .get();

                name = Objects.requireNonNull(document.getElementById("pagetitle")).text();

                Elements prices = document.getElementsByClass("dinamic_info_wrapper");
                price = Double.parseDouble(prices.get(0).getElementsByClass("price font-bold font_mxs").get(0).attr("data-value"));
                if (prices.get(0).children().hasClass("product-inner-price-old")) {
                    String oldPriceText = prices.get(0).getElementsByClass("product-inner-price-old").get(0).child(0).text();
                    oldPrice = Double.parseDouble(oldPriceText.substring(0, oldPriceText.indexOf("руб")).trim());
                }

                Elements features = document.getElementsByClass("properties__item properties__item--compact font_xs");
                for (Element element : features) {
                    if (element.getElementsByClass("properties__title muted properties__item--inline").get(0).text().contains("Торговая марка")) {
                        brand = element.getElementsByClass("properties__value darken properties__item--inline").get(0).text().trim();
                        updateMessage("Получение торговой марки по ссылке " + link);
                        continue;
                    }
                    if (element.getElementsByClass("properties__title muted properties__item--inline").get(0).text().contains("Штрихкод")) {
                        barcode = Long.parseLong(element.getElementsByClass("properties__value darken properties__item--inline").get(0).text().trim());
                        updateMessage("Получение штрихкода по ссылке " + link);
                        continue;
                    }
                    if (element.getElementsByClass("properties__title muted properties__item--inline").get(0).text().contains("Тип")) {
                        type = element.getElementsByClass("properties__value darken properties__item--inline").get(0).text().trim();
                        updateMessage("Получение типа по ссылке " + link);
                        continue;
                    }
                    if (element.getElementsByClass("properties__title muted properties__item--inline").get(0).text().contains("Размер")) {
                        size = element.getElementsByClass("properties__value darken properties__item--inline").get(0).text().trim();
                        updateMessage("Получение размера по ссылке " + link);
                        continue;
                    }
                    if (element.getElementsByClass("properties__title muted properties__item--inline").get(0).text().contains("Номинальное количество")) {
                        String countText = element.getElementsByClass("properties__value darken properties__item--inline").get(0).text().trim();
                        if (countText.toLowerCase().contains("x") || countText.toLowerCase().contains("х")) {
                            countText = countText.replaceAll("\\D", " ").trim();
                            int multiplier1 = Integer.parseInt(countText.substring(0, countText.indexOf(" ")).trim());
                            int multiplier2 = Integer.parseInt(countText.substring(countText.indexOf(" ")).trim());

                            countInPackage = multiplier1 * multiplier2;
                            continue;
                        }if (countText.contains("+")){
                            countText = countText.replaceAll("\\D", " ").trim();
                            int term1 = Integer.parseInt(countText.substring(0, countText.indexOf(" ")).trim());
                            int term2 = Integer.parseInt(countText.substring(countText.indexOf(" ")).trim());

                            countInPackage = term1 + term2;
                        }else {
                            countText = countText.replaceAll("\\D", " ").trim();
                            countInPackage = Integer.parseInt(countText);
                            updateMessage("Получение количества по ссылке " + link);
                        }

                    }
                }

                nomenclatures.add(new Nomenclature(name, link, brand, barcode, type, size, countInPackage, price, oldPrice));
                updateMessage("Создание объекта с полученных данных");
                updateProgress(100 + i, 10000);
                i += pathCat;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try (XSSFWorkbook workbook = createExcel(nomenclatures)) {
            FileOutputStream fos = new FileOutputStream(path);
            workbook.write(fos);
            fos.close();
            updateMessage("Сохранение файла по пути " + path);
            updateProgress(10000, 10000);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void getLinks(String category) throws IOException {
        String baseUrl = "https://ostrov-shop.by";

        Document document = Jsoup.connect(baseUrl + category)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
                .timeout(5000)
                .ignoreContentType(true)
                .ignoreHttpErrors(true)
                .referrer("https://www.google.com/").get();

        Elements elements = document.getElementsByClass("dinamic_info_wrapper");
        updateMessage("Получение ссылок на страницы с товаром");
        elements.forEach(item -> links.add(baseUrl + item.getElementsByClass("item-title title-heigh").get(0).child(0).attr("href")));

        boolean isNext = !document.getElementsByClass("nums").isEmpty();
        if (isNext) {
            Element nextElement = document.getElementsByClass("nums").get(0);
            if (!nextElement.getElementsByClass("flex-next").isEmpty()){
                getLinks(nextElement.getElementsByClass("flex-next").get(0).attr("href"));
            }
        }
    }

    private XSSFWorkbook createExcel(ArrayList<Nomenclature> nomenclatures) {
        updateMessage("Создание excel файла...");
        updateProgress(9100, 10000);

        int startRow = 1;
        int startColumn = 1;

        //Создаю книгу excel
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFDataFormat df = wb.createDataFormat();
        CreationHelper creationHelper = wb.getCreationHelper();
        XSSFSheet sheet = wb.createSheet("Результаты");

        //стиль ячеек шапки
        CellStyle headStyle = wb.createCellStyle();
        headStyle.setAlignment(HorizontalAlignment.CENTER);
        headStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headStyle.setBorderBottom(BorderStyle.MEDIUM);
        headStyle.setBorderLeft(BorderStyle.MEDIUM);
        headStyle.setBorderTop(BorderStyle.MEDIUM);
        headStyle.setBorderRight(BorderStyle.MEDIUM);

        // стиль для основной табличной части
        CellStyle contentDecimalStyle = wb.createCellStyle();
        contentDecimalStyle.setVerticalAlignment(VerticalAlignment.TOP);
        contentDecimalStyle.setBorderBottom(BorderStyle.THIN);
        contentDecimalStyle.setBorderLeft(BorderStyle.THIN);
        contentDecimalStyle.setBorderTop(BorderStyle.THIN);
        contentDecimalStyle.setBorderRight(BorderStyle.THIN);
        contentDecimalStyle.setDataFormat(df.getFormat("0.00"));

        // стиль для основной табличной части
        CellStyle contentStyle = wb.createCellStyle();
        contentStyle.setVerticalAlignment(VerticalAlignment.TOP);
        contentStyle.setBorderBottom(BorderStyle.THIN);
        contentStyle.setBorderLeft(BorderStyle.THIN);
        contentStyle.setBorderTop(BorderStyle.THIN);
        contentStyle.setBorderRight(BorderStyle.THIN);
        contentStyle.setDataFormat(df.getFormat("#"));

        //шрифты шапки
        Font headFont = wb.createFont();
        headFont.setFontName("Times New Roman");
        headFont.setBold(true);
        headFont.setFontHeightInPoints((short) 12);
        headStyle.setFont(headFont);

        XSSFRow head = sheet.createRow(startRow);

        XSSFCell nameHead = head.createCell(startColumn);
        nameHead.setCellValue("Наименование");
        headStyle.setWrapText(true);
        nameHead.setCellStyle(headStyle);

        XSSFCell artHead = head.createCell(++startColumn);
        artHead.setCellValue("Бренд");
        headStyle.setWrapText(true);
        artHead.setCellStyle(headStyle);

        XSSFCell barcodeHead = head.createCell(++startColumn);
        barcodeHead.setCellValue("Штрихкод");
        headStyle.setWrapText(true);
        barcodeHead.setCellStyle(headStyle);

        XSSFCell typeHead = head.createCell(++startColumn);
        typeHead.setCellValue("Тип");
        headStyle.setWrapText(true);
        typeHead.setCellStyle(headStyle);

        XSSFCell sieHead = head.createCell(++startColumn);
        sieHead.setCellValue("Размер");
        headStyle.setWrapText(true);
        sieHead.setCellStyle(headStyle);

        XSSFCell countInPackageHead = head.createCell(++startColumn);
        countInPackageHead.setCellValue("Количество в упаковке, шт");
        headStyle.setWrapText(true);
        countInPackageHead.setCellStyle(headStyle);

        XSSFCell priceHead = head.createCell(++startColumn);
        priceHead.setCellValue("Цена, руб.");
        headStyle.setWrapText(true);
        priceHead.setCellStyle(headStyle);

        XSSFCell priceOneHead = head.createCell(++startColumn);
        priceOneHead.setCellValue("Цена за 1 шт, руб.");
        headStyle.setWrapText(true);
        priceOneHead.setCellStyle(headStyle);

        XSSFCell oldPriceHead = head.createCell(++startColumn);
        oldPriceHead.setCellValue("Старая цена, руб.");
        headStyle.setWrapText(true);
        oldPriceHead.setCellStyle(headStyle);

        XSSFCell discountHead = head.createCell(++startColumn);
        discountHead.setCellValue("Скидка, %");
        headStyle.setWrapText(true);
        discountHead.setCellStyle(headStyle);

        sheet.setAutoFilter(CellRangeAddress.valueOf("B2:K2"));
        sheet.createFreezePane(0, 2);

        updateProgress(9300, 10000);

        int startRowContent = startRow + 1;

        long pathNom = 600 / nomenclatures.size();
        long i = pathNom;

        for (Nomenclature nom : nomenclatures) {
            updateMessage("Внесение данных по " + nom.name() + " в файл excel");

            int startColumnContent = 1;

            XSSFRow content = sheet.createRow(startRowContent);

            //Наименование товара в excel
            XSSFCell cellName = content.createCell(startColumnContent);
            contentStyle.setWrapText(true);
            cellName.setCellValue(nom.name());
            XSSFHyperlink hl = (XSSFHyperlink) creationHelper.createHyperlink(HyperlinkType.URL);
            hl.setAddress(nom.link());
            cellName.setHyperlink(hl);
            cellName.setCellStyle(contentStyle);
            sheet.setColumnWidth(startColumnContent, 9300);

            //Бренд
            XSSFCell brand = content.createCell(++startColumnContent);
            contentStyle.setWrapText(true);
            brand.setCellValue(nom.brand());
            brand.setCellStyle(contentStyle);
            sheet.setColumnWidth(startColumnContent, 4138);

            //Штрихкод
            XSSFCell barcode = content.createCell(++startColumnContent);
            contentStyle.setWrapText(true);
            barcode.setCellValue(nom.barcode());
            barcode.setCellStyle(contentStyle);
            sheet.setColumnWidth(startColumnContent, 4833);

            //Тип
            XSSFCell type = content.createCell(++startColumnContent);
            contentStyle.setWrapText(true);
            type.setCellValue(nom.type());
            type.setCellStyle(contentStyle);
            sheet.setColumnWidth(startColumnContent, 3808);

            //Размер
            XSSFCell size = content.createCell(++startColumnContent);
            contentStyle.setWrapText(true);
            size.setCellValue(nom.size());
            size.setCellStyle(contentStyle);

            //Количество в упаковке
            XSSFCell count = content.createCell(++startColumnContent);
            contentStyle.setWrapText(true);
            count.setCellValue(nom.countInPackage());
            count.setCellStyle(contentStyle);
            sheet.setColumnWidth(startColumnContent, 3991);

            //Цена
            XSSFCell price = content.createCell(++startColumnContent);
            contentStyle.setWrapText(true);
            price.setCellValue(nom.price());
            price.setCellStyle(contentDecimalStyle);

            //Цена за 1 шт
            XSSFCell priceOne = content.createCell(++startColumnContent);
            contentStyle.setWrapText(true);
            if (nom.price() > 0 && nom.countInPackage() > 0) {
                priceOne.setCellValue(nom.price() / nom.countInPackage());
            }
            priceOne.setCellStyle(contentDecimalStyle);

            //Старая цена
            XSSFCell oldPrice = content.createCell(++startColumnContent);
            contentStyle.setWrapText(true);
            if (nom.oldPrice() > 0) {
                oldPrice.setCellValue(nom.oldPrice());
            }
            oldPrice.setCellStyle(contentDecimalStyle);

            //Скидка
            XSSFCell discount = content.createCell(++startColumnContent);
            contentStyle.setWrapText(true);
            if (nom.oldPrice() > 0) {
                double disc = (1 - nom.price() / nom.oldPrice()) * 100;
                discount.setCellValue(disc);
            }
            discount.setCellStyle(contentDecimalStyle);

            startRowContent++;
            updateProgress(9300 + i, 10000);
            i += pathNom;

        }
        return wb;
    }
}
