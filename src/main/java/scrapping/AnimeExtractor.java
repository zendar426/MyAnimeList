package scrapping;

import com.gargoylesoftware.htmlunit.html.Html;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import scrapping.Media.DetailedMedia.AnimeMedia;
import scrapping.Media.MediaManager;
import scrapping.Media.Preview.AnimePreviewSearch;
import scrapping.Media.Preview.AnimePreviewTop;

import javax.imageio.ImageReader;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

public class AnimeExtractor extends Extractor{


    protected List<HtmlElement> emissionDataFromTop;
    protected List<String> openingRows,endingRows;
    //protected List<AnimePreviewTop> previewsList;

    public AnimeExtractor() {
        //previewsList=new ArrayList<>();
        anchorXpathRef=AnimeXpaths.relHrefToAnimeInTop.xpath;
        typeOfMediaUrl+="anime/";
        searchCat="&cat=anime";
        numeroPaginaEnTop=1;
        topURL+="topanime.php";
        searchType="anime.php?q=";
        searchRowXpath=AnimeXpaths.relAnimeSearchResultRows.xpath;
        categoriasList=Arrays.asList("TV","OVA","Movie","ONA","Special");

    }

    public void collectFromTop(){
        setupTopPage(topURL);
        extractTopTags();
        getAnchors();

        //client.close();
    }
    public void collectFromTop(String targetURL){
        setupTopPage(targetURL);
        extractTopTags();
        getAnchors();

        //client.close();
    }

    public void pasarPreviewsAMediaManager(){
        List<AnimePreviewTop> tempPreviewsList=formarPreviewsPagTop();
        tempPreviewsList.stream().forEach(MediaManager::agregarAnimePreviewTopALista);
    }


    public List<AnimePreviewTop> formarPreviewsPagTop(){
        List<AnimePreviewTop> tempPreviewsList=new ArrayList<>();
        topRowsOfMedia.stream().forEach(animeRow->tempPreviewsList.add(formarRecordPreview(animeRow)));
        return tempPreviewsList;
    }

    public AnimePreviewTop formarRecordPreview(HtmlElement animeRow){
        String urlAnime=getHrefFromAnchor(animeRow);

        return new AnimePreviewTop(obtenerID(urlAnime),obtenerNombreAnimePreview(animeRow),obtenerCategoriaAnime(animeRow),obtenerNumeroRank(animeRow),obtenerNumeroPuntos(animeRow),urlAnime);
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

    public void obtenerInformacionImportanteAnime(){
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
        System.out.println(rawInformationElements.size());
        HtmlElement elementoBuscado =(HtmlElement) articleTags.get(0).getByXPath(AnimeXpaths.relAnimeImportantGeneralInfoDetails.xpath).get(0);
        System.out.println(rawInformationElements);

        return rawInformationElements.indexOf(elementoBuscado);
    }

    public int buscarPosicionFinInformacionRelevante(){
        HtmlElement elementoBuscado =(HtmlElement) articleTags.get(0).getByXPath(AnimeXpaths.relAnimeEndofImportantGeneralInfoDetails.xpath).get(0);
        //List<HtmlElement> elementoBuscado=new ArrayList<>(elementoClave.getByXPath(Xpaths.relAnimeImportantGeneralInfoDetails.xpath));
        //System.out.println(elementoBuscado.get(0).getVisibleText());

        return rawInformationElements.indexOf(elementoBuscado);
    }



    public Map<String,String> ponerInfoImportanteEnMaps(List<HtmlElement> listaInfoImportante){
        Map<String,String> importantInfoPairs=new HashMap<>();
        usableInformationElements.stream().forEach(importantInfoRow->
        {
            String[] separatedImportantInfo =importantInfoRow.asNormalizedText().split(":",2);
            importantInfoPairs.put(separatedImportantInfo[0], separatedImportantInfo[1]);

        });
        return importantInfoPairs;
    }


    public void mostrarInformacionImportante(){
        usableInformationElements.stream().forEach(infoRow-> System.out.println(infoRow.getVisibleText()));
    }

    /*public void getPreviewsTodos(){
        previewsList.stream().forEach(preview-> System.out.println("["+preview.posicionRanking()+"] "+preview));
    }*/

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

    public Map<String,String> extraerDatosObrasRelacionadas(HtmlElement articulo){
        if (contieneObrasRelacionadas(articulo)){
            Map<String,String> relMediaMap=obtenerObrasRelacionadasEnMap(articulo);
            return relMediaMap;
        }
        else{
            return null;
        }
    }


    private Map<String,String> obtenerObrasRelacionadasEnMap(HtmlElement articulo){
        Map<String,String> tempRelMediaMap=new HashMap<>();
        List<HtmlElement> tempRelatedMediaList= articulo.getByXPath(AnimeXpaths.relAnimeDetailsRelatedMediaTable.xpath);
        tempRelatedMediaList.stream()
                .forEach(relatedMediaType->
                {
                    //System.out.println("relatedMediaType.asNormalizedText() = " + relatedMediaType.asNormalizedText());
                    String[] splitRelMedia=relatedMediaType.asNormalizedText().split(":",2);
                    tempRelMediaMap.put(splitRelMedia[0],splitRelMedia[1]);
                });
        return tempRelMediaMap;
    }


    public List<String> obtenerEmisorasDelAnime(){
        List<HtmlElement> emisoras=articleTags.get(0).getByXPath(AnimeXpaths.relAnimeDetailsRowBroadcast.xpath);
        List<String> stringEmisoras=new ArrayList<>();
        if (tieneEmisoras(emisoras)){
            emisoras.stream().forEach(emisoraElement-> stringEmisoras.add(emisoraElement.asNormalizedText()));
            return stringEmisoras;
        }
        else {
            return null;
        }
    }

    public List<String> obtenerEmisorasDelAnime(HtmlElement anime){
        List<HtmlElement> emisoras=anime.getByXPath(AnimeXpaths.relAnimeDetailsBroadcasters.xpath);
        List<String> stringEmisoras=new ArrayList<>();
        if (tieneEmisoras(emisoras)){
            emisoras.stream().forEach(emisoraElement-> stringEmisoras.add(emisoraElement.asNormalizedText()));
            return stringEmisoras;
        }
        else {
            return null;
        }
    }
    private boolean tieneEmisoras(List<HtmlElement> emisoras){
        return !emisoras.isEmpty();
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

    public AnimeMedia crearAnimeDetalles(AnimePreviewTop preview){

        return new AnimeMedia(preview);

    }

    public void obtenerDetallesDeEmision(AnimeMedia objetivo){
        usableInformationElements.stream().forEach(infoRow->{
            String[] infoPair=infoRow.getVisibleText().split(":",0);
            agregarAHashMapAnime(objetivo,infoPair);

        });

    }
    public void agregarAHashMapAnime(AnimeMedia anime, String[] detallesSeparados){
        try {
            anime.agregarInfoEmision(detallesSeparados[0],detallesSeparados[1]);
        }
        catch (IndexOutOfBoundsException exception){
            System.err.println("No existe un par en "+Arrays.toString(detallesSeparados));
        }
    }

    public void formarAnimeDetalle(){

    }


    public void extraerVariasPaginasTop(int numPaginas){
        IntStream.range(1,numPaginas+1).forEach(pageNumber->{
            String urlObjetivo=baseSearchUrl+convertirPaginaTopAUrl(pageNumber);
            collectFromTop(urlObjetivo);
        });
    }

    public void pasarTodasFilasAPreview(){
        List<AnimePreviewSearch> searchPreviewList=new ArrayList<>();
        searchRowsOfMedia.stream().forEach(searchRow->searchPreviewList.add(pasarFilaSearchAPreview(searchRow)));
        System.out.println(searchPreviewList);


    }

    public AnimePreviewSearch pasarFilaSearchAPreview(HtmlElement filaBusqueda){
        String url=obtenerLinkBusqueda(filaBusqueda);
        int id=obtenerID(url);
        String tipoEmision=obtenerTipoEmisionBusqueda(filaBusqueda);
        String nombre=obtenerNombreBusqueda(filaBusqueda);
        double puntuacion=obtenerPuntajeBusqueda(filaBusqueda);
        return new AnimePreviewSearch(id,nombre,tipoEmision,puntuacion,url);

    }

    public String obtenerNombreBusqueda(HtmlElement filaBusqueda){
        String tempNombre=((HtmlElement) filaBusqueda.getFirstByXPath(AnimeXpaths.relAnimeSearchTitle.xpath)).asNormalizedText();
        tempNombre=tempNombre.replace(" add","");
        return tempNombre;
    }

    public String obtenerLinkBusqueda(HtmlElement filaBusqueda){

        return ((HtmlAnchor) filaBusqueda.getFirstByXPath(AnimeXpaths.relAnimeSearchHref.xpath)).getHrefAttribute();
    }


    public double obtenerPuntajeBusqueda(HtmlElement filaBusqueda){
        String puntajeString=((HtmlElement) filaBusqueda.getFirstByXPath(AnimeXpaths.relAnimeSearchScore.xpath)).asNormalizedText();
        return puntajeString.equals("N/A")?0.0:Double.parseDouble(puntajeString);
    }

    public String obtenerTipoEmisionBusqueda(HtmlElement filaBusqueda){
        return ((HtmlElement) filaBusqueda.getFirstByXPath(AnimeXpaths.relAnimeSearchEmissionType.xpath)).asNormalizedText();
    }



}

