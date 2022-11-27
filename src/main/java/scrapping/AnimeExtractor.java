package scrapping;

import com.gargoylesoftware.htmlunit.html.Html;
import com.gargoylesoftware.htmlunit.html.HtmlElement;

import javax.imageio.ImageReader;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AnimeExtractor extends Extractor{

    String topURL=baseSearchUrl+"topanime.php";
    String searchURL;
    List<HtmlElement> emissionDataFromTop;
    List<HtmlElement> animeRowData;
    List<String> openingRows,endingRows;
    List<AnimePreview> previewsList;

    String animexpathTitleRowsInTop="//html/body/div[1]/div[2]/div[3]/div[2]/div[4]/table/tbody/tr[@class=\"ranking-list\"]",animexpathTitleLinkFromTop="td[2]/div/div[2]/h3/a";

    public AnimeExtractor() {
        previewsList=new ArrayList<>();
    }

    public void collectFromTop(){
        setupTopPage(topURL);
        extractTopTags();
        getAnchors();

        //client.close();
    }


    public void formarPreviewsPagTop(){
        topRowsOfMedia.stream().forEach(animeRow->previewsList.add(formarRecordPreview(animeRow)));


    }

    public AnimePreview formarRecordPreview(HtmlElement animeRow){

        return new AnimePreview(obtenerNombreAnimePreview(animeRow),obtenerCategoriaAnime(animeRow),obtenerNumeroRank(animeRow),obtenerNumeroPuntos(animeRow),getHrefFromAnchor(animeRow));
    }

    public String obtenerNombreAnimePreview(HtmlElement animePreview){
        return ((HtmlElement) animePreview.getFirstByXPath(AnimeXpaths.relTitleAnimeInTop.xpath)).getVisibleText();
    }


    public String obtenerCategoriaAnime(HtmlElement elementoInteres){
        getAnimeEmissionDetailsFromTop(elementoInteres);
        String datosEmision=emissionDataFromTop.get(0).getVisibleText();
        return definirCategoria(datosEmision);
    }


    public void getAnimeEmissionDetailsFromTop(HtmlElement elementoInteres){
        emissionDataFromTop=new ArrayList<>(elementoInteres.getByXPath(AnimeXpaths.relEmissionDetailsAnimeInTop.xpath));
    }


    public String definirCategoria(String datosEmision){
        List<String> categoriasList= Arrays.asList(new String[]{"TV","OVA","Movie","ONA"});
        return categoriasList.stream()
                .filter(categoriaIndiv->datosEmision.contains(categoriaIndiv))
                .findFirst()
                .get();
    }


    public int obtenerNumeroRank(HtmlElement animePreview){
        List<HtmlElement> templist=new ArrayList<>(animePreview.getByXPath(AnimeXpaths.relRankingNumberAnimeInTop.xpath));
        return Integer.parseInt(templist.get(0).getVisibleText());
    }

    public double obtenerNumeroPuntos(HtmlElement animePreview){
        List<HtmlElement> templist=new ArrayList<>(animePreview.getByXPath(AnimeXpaths.relScoreNumberAnimeInTop.xpath));
        return Double.parseDouble(templist.get(0).getVisibleText());
    }

    public void obtenerInformacionImportanteElemSeleccionado(int seleccion){
        extractDataFromArticle(articlesURLs.get(seleccion));
        obtenerInformacionImportanteAnime();
    }

    private void obtenerInformacionImportanteAnime(){
        getAnimeImportantInformationRaw();
        formarListaInfoImportante();
    }


    public void getAnimeImportantInformationRaw(){
        rawInformationElements =new ArrayList<>(articleTags.get(0).getByXPath(AnimeXpaths.relAnimeDetailsGeneralInfo.xpath));
    }

    public void formarListaInfoImportante(){
        int inicio,fin;
        inicio=buscarPosicionInicioInformacionRelevante()+1;
        fin=buscarPosicionFinInformacionRelevante()-1; //exclusive y evitar incluir el <br>
        usableInformationElements = rawInformationElements.subList(inicio,fin);
    }


    public int buscarPosicionInicioInformacionRelevante(){
        System.out.println(rawInformationElements.size());;
        HtmlElement elementoBuscado =(HtmlElement) articleTags.get(0).getByXPath(AnimeXpaths.relAnimeImportantGeneralInfoDetails.xpath).get(0);
        System.out.println(rawInformationElements);
        //List<HtmlElement> elementoBuscado=new ArrayList<>(elementoClave.getByXPath(Xpaths.relAnimeImportantGeneralInfoDetails.xpath));
        //System.out.println(elementoBuscado.get(0).getVisibleText());

        return rawInformationElements.indexOf(elementoBuscado);
    }

    public int buscarPosicionFinInformacionRelevante(){
        HtmlElement elementoBuscado =(HtmlElement) articleTags.get(0).getByXPath(AnimeXpaths.relAnimeEndofImportantGeneralInfoDetails.xpath).get(0);
        //List<HtmlElement> elementoBuscado=new ArrayList<>(elementoClave.getByXPath(Xpaths.relAnimeImportantGeneralInfoDetails.xpath));
        //System.out.println(elementoBuscado.get(0).getVisibleText());

        return rawInformationElements.indexOf(elementoBuscado);
    }



    public void ponerInfoImportanteEnMaps(){
        //usableInformationElements.stream().map()
    }


    public void mostrarInformacionImportante(){
        usableInformationElements.stream().forEach(infoRow-> System.out.println(infoRow.getVisibleText()));
    }

    public String getAnimeNameFromTop(HtmlElement elemento){
        return ((HtmlElement) elemento.getByXPath(AnimeXpaths.relTitleAnimeInTop.xpath).get(0)).getVisibleText();
    }

    public void getPreviewsTodos(){
        previewsList.stream().forEach(preview-> System.out.println("["+preview.posicionRanking()+"] "+preview));
    }

    public void obtenerYMostrarImagenPreview(HtmlElement columna){
        obtenerImagen(columna);
        mostrarImagen();
    }

    public void obtenerImagen(HtmlElement columna){
        previewImage=columna.getFirstByXPath(AnimeXpaths.relPreviewCoverImageInTop.xpath);
    }

    public void mostrarImagen(){
        try {
            ImageReader imgreader = previewImage.getImageReader();
            BufferedImage bimage =imgreader.read(0);
        }
        catch (IOException e){

        }
    }

    public void extraerDatosObrasRelacionadas(HtmlElement articulo){
        if (contieneObrasRelacionadas(articulo)){

        }
        else{

        }

    }

    public boolean contieneObrasRelacionadas(HtmlElement articulo){
        return !articulo.getByXPath(AnimeXpaths.relAnimeDetailsRelatedMediaTable.xpath).isEmpty();

    }

    public void extraerMusica(HtmlElement article){
        openingRows=new ArrayList<>();
        endingRows=new ArrayList<>();
        extractOpenings(article);
        extractEndings(article);

    }
    public void extractOpenings(HtmlElement article){
        extraerTableOpenings(article);
    }

    public void extractEndings(HtmlElement article){
        extraerTableEndings(article);
    }

    public void extraerTableOpenings(HtmlElement article){
        ArrayList<HtmlElement> openingsTable=new ArrayList<>(article.getByXPath(AnimeXpaths.relAnimeDetailsOpeningsTable.xpath));
        extraerFilasOpenings(openingsTable.get(0));
    }
    public void extraerFilasOpenings(HtmlElement tablaOpenings){
        //openingRows.addAll(tablaOpenings.getByXPath(AnimeXpaths.relAnimeDetailsOpeningsRows.xpath));
        ArrayList<HtmlElement> tempOpeningRows =new ArrayList<>(tablaOpenings.getByXPath(AnimeXpaths.relAnimeDetailsOpeningsRows.xpath));
        tempOpeningRows.stream().forEach(opRow-> openingRows.add(opRow.getVisibleText()));
    }

    public void extraerTableEndings(HtmlElement article){
        HtmlElement endingsTable=article.getFirstByXPath(AnimeXpaths.relAnimeDetailsEndingsTable.xpath);
        extraerFilasEndings(endingsTable);

    }

    public void extraerFilasEndings(HtmlElement tablaEndings){
        //openingRows.addAll(tablaEndings.getByXPath(AnimeXpaths.relAnimeDetailsOpeningsRows.xpath));
        ArrayList<HtmlElement> tempEndingRows =new ArrayList<>(tablaEndings.getByXPath(AnimeXpaths.relAnimeDetailsEndingsRows.xpath));
        tempEndingRows.stream().forEach(edRow -> endingRows.add(edRow.getVisibleText()));
    }



    public String convertirPaginaTopAUrl(int paginas){
        //String numeroPag=paginas==1?"":"?limit="+((paginas-1)*50);
        return paginas==1?"":"?limit="+((paginas-1)*50);
    }



    public void createSearchURL(String searchTerm){
        searchURL=baseSearchUrl+"anime.php?q="+searchTerm+"&cat=anime";
        System.out.println(searchURL);
    }
}
