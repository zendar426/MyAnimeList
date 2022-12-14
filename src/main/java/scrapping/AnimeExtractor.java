package scrapping;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import err.ExcepcionDeConexion;
import err.ExcepcionMalFormatoURL;
import err.ExcepcionPaginaNoPreparada;
import scrapping.Media.DetailedMedia.AnimeMedia;
import scrapping.Media.Preview.AnimePreview;

import javax.imageio.ImageReader;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

public class AnimeExtractor extends Extractor{


    protected List<HtmlElement> emissionDataFromTop;
    protected List<String> openingRows,endingRows;


    public List<AnimePreview> animeSearchPreview,animeTopPreview;
    public List<AnimeMedia> animeMediaList;


    public AnimeExtractor() {
        //previewsList=new ArrayList<>();
        anchorXpathRef=AnimeXpaths.relHrefToAnimeInTop.xpath;
        typeOfMediaUrl+="anime/";
        searchCat="&cat=anime";
        animeMediaList=new ArrayList<>();
        numeroPaginaEnTop=1;
        topURL+="topanime.php";
        searchType="anime.php?q=";
        searchRowXpath=AnimeXpaths.relAnimeSearchResultRows.xpath;
        categoriasList=Arrays.asList("TV","OVA","Movie","ONA","Special");

    }


    public void iniciarScrapper(){
        try {
            collectFromTopAndFormPreviews();
        }
        catch (ExcepcionDeConexion conEx){
            System.out.println(conEx);
        }
        catch (ExcepcionMalFormatoURL malURL){
            System.out.println(malURL);
        }
    }

    public void paginaTopAdelante(){
        try {
            avanzarPaginaTop();
            collectFromTopAndFormPreviews(pageTopURL);
        }
        catch (ExcepcionDeConexion conEx){
            System.out.println(conEx);
        }
        catch (ExcepcionMalFormatoURL malUrl){
            System.out.println(malUrl);

        }
    }

    public void paginaTopAtras(){
        try {
            if (numeroPaginaEnTop!=1){
                regresarPaginaTop();
                collectFromTopAndFormPreviews(pageTopURL);
            }
        }
        catch (ExcepcionDeConexion conEx){
            System.out.println(conEx);
        }
        catch (ExcepcionMalFormatoURL malUrl){
            System.out.println(malUrl);
        }
    }

    AnimePreview testsSeleccionarPreviewMedianteInt(int seleccion){
        try{
            return animeTopPreview.get(seleccion);
        }
        catch (NullPointerException e){
            return null;
        }
    }

    public void seleccionarPreviewTopParaMostrarDetalles(AnimePreview preview){
        try {
            formarDetallesAnimeDeTop(preview);
        }
        catch (ExcepcionDeConexion conEx){
            System.out.println(conEx);
        }
        catch (ExcepcionMalFormatoURL malUrl){
            System.out.println(malUrl);
        }
        catch (ExcepcionPaginaNoPreparada noPrep){
            System.out.println(noPrep+" en seleccionar preview top");
        }
    }

    private void formarDetallesAnimeDeTop(AnimePreview preview) throws ExcepcionDeConexion, ExcepcionMalFormatoURL, ExcepcionPaginaNoPreparada {
        usePreviewToCreateDetailsArticle(preview);
        AnimeMedia nuevoDetalles = safeCrearAnimeDetalles(preview);
        obtenerInformacionImportanteAnime();
        obtenerDetallesDeEmision(nuevoDetalles);
        addMusicDataToAnime(nuevoDetalles);
        nuevoDetalles.setEmisoras(obtenerEmisorasDelAnime());
        agregarInfoStaffAdetalles(nuevoDetalles);
        animeMediaList.add(nuevoDetalles);

    }

    public void seleccionarPreviewSearchParaMostrarDetalles(AnimePreview preview){
        try {
            formarDetallesAnimeDeBusqueda(preview);
        }
        catch (ExcepcionDeConexion conEx){
            System.out.println(conEx);
        }
        catch (ExcepcionMalFormatoURL malUrl){
            System.out.println(malUrl);
        }
        catch (ExcepcionPaginaNoPreparada noPrep){
            System.out.println(noPrep+" en seleccionar preview busqueda");
        }
    }

    private void formarDetallesAnimeDeBusqueda(AnimePreview previewBusqueda) throws ExcepcionDeConexion, ExcepcionMalFormatoURL, ExcepcionPaginaNoPreparada {
        usePreviewToCreateDetailsArticle(previewBusqueda);
        rankPosFromDetails=obtenerPosicionRankingDetalles();
        AnimeMedia nuevoDetalles= safeCrearAnimeDetalles(previewBusqueda);
        obtenerInformacionImportanteAnime();
        obtenerDetallesDeEmision(nuevoDetalles);
        addMusicDataToAnime(nuevoDetalles);
        nuevoDetalles.setEmisoras(obtenerEmisorasDelAnime());
        agregarInfoStaffAdetalles(nuevoDetalles);
        animeMediaList.add(nuevoDetalles);
    }

    public void collectFromTopAndFormPreviews() throws ExcepcionDeConexion, ExcepcionMalFormatoURL {
            collectFromTop();
            animeTopPreview=new ArrayList<>(formarPreviewsPagTop());
    }
    public void collectFromTopAndFormPreviews(String url) throws ExcepcionDeConexion, ExcepcionMalFormatoURL {
        collectFromTop(url);
        animeTopPreview=new ArrayList<>(formarPreviewsPagTop());
    }

    public void collectFromSearchAndFormPreviews(String searchTerm){
        try{
            createSearchURL(searchTerm);
            realizarBusqueda();
            animeSearchPreview=new ArrayList<>(pasarTodasFilasBusquedaApreview());
        }
        catch (ExcepcionDeConexion excCon){
            System.out.println(excCon);
        }
        catch(ExcepcionMalFormatoURL malURL){
            System.out.println(malURL);
        }
    }


    private void collectFromTop() throws ExcepcionDeConexion, ExcepcionMalFormatoURL {
            setupTopPage(topURL);
            extractTopTags();
            getAnchors();
        //client.close();
    }
    private void collectFromTop(String targetURL) throws ExcepcionDeConexion, ExcepcionMalFormatoURL {
            setupTopPage(targetURL);
            extractTopTags();
            getAnchors();
        //client.close();
    }



    public List<AnimePreview> formarPreviewsPagTop() {

            return agregarPreviewsATempList();

    }
    public List<AnimePreview> agregarPreviewsATempList() {
            List<AnimePreview> tempPreviewsList = new ArrayList<>();
            topRowsOfMedia.stream()
                    .forEach(animeRow -> tempPreviewsList.add(formarRecordPreview(animeRow)));
            return tempPreviewsList;
    }

    public AnimePreview formarRecordPreview(HtmlElement animeRow) {
        String urlAnime=getHrefFromAnchor(animeRow);
        return new AnimePreview(obtenerID(urlAnime),obtenerNombreAnimePreview(animeRow),obtenerCategoriaAnime(animeRow),obtenerNumeroRank(animeRow),obtenerNumeroPuntos(animeRow),urlAnime);
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


    public int obtenerPosicionRankingDetalles(){
        String rankPosAsText=((HtmlElement) articleTags.getFirstByXPath(AnimeXpaths.relAnimeDetailsRank.xpath)).asNormalizedText();
        rankPosAsText=rankPosAsText.replace("#","");

        return Integer.parseInt(rankPosAsText);
    }

    public void obtenerInformacionImportanteAnime(){
        getAnimeImportantInformationRaw();
        formarListaInfoImportante();
    }


    public void getAnimeImportantInformationRaw(){
        rawInformationElements =articleTags.getByXPath(AnimeXpaths.relAnimeDetailsGeneralInfo.xpath);
    }

    public void formarListaInfoImportante(){
        int inicio,fin;
        inicio=buscarPosicionInicioInformacionRelevante()+1;
        fin=buscarPosicionFinInformacionRelevante()-1; //exclusive y evitar incluir el <br>
        usableInformationElements = rawInformationElements.subList(inicio,fin);
    }


    public int buscarPosicionInicioInformacionRelevante(){
        System.out.println(rawInformationElements.size());
        HtmlElement elementoBuscado =(HtmlElement) articleTags.getByXPath(AnimeXpaths.relAnimeImportantGeneralInfoDetails.xpath).get(0);
        System.out.println(rawInformationElements);

        return rawInformationElements.indexOf(elementoBuscado);
    }

    public int buscarPosicionFinInformacionRelevante(){
        HtmlElement elementoBuscado =(HtmlElement) articleTags.getByXPath(AnimeXpaths.relAnimeEndofImportantGeneralInfoDetails.xpath).get(0);
        return rawInformationElements.indexOf(elementoBuscado);
    }



    public Map<String,String> ponerInfoImportanteEnMaps(){
        Map<String,String> importantInfoPairs=new HashMap<>();
        usableInformationElements.stream()
                .forEach(importantInfoRow->
                {
                    String[] separatedImportantInfo =importantInfoRow.asNormalizedText().split(":",2);
                    importantInfoPairs.put(separatedImportantInfo[0], separatedImportantInfo[1]);
                });
        return importantInfoPairs;
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



    public Map<String,String> extraerDatosObrasRelacionadas(HtmlElement articulo) {
        if (contieneObrasRelacionadas(articulo)){
                return crearMapObrasRelacionadas(articulo);
        }
        else{
            Map noneHashmap=new HashMap<>();
            noneHashmap.put("none","none");
            return noneHashmap;
        }
    }


    private Map<String,String> crearMapObrasRelacionadas(HtmlElement articulo) {
            Map<String,String> relMediaMap=obtenerObrasRelacionadasEnMap(articulo);
            return relMediaMap;
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
        List<HtmlElement> emisoras=extraerTagsEmisora();
        List<String> stringEmisoras=new ArrayList<>();
        if (tieneEmisoras(emisoras)){
            emisoras.stream()
                    .forEach(emisoraElement-> stringEmisoras.add(emisoraElement.asNormalizedText()));
            return stringEmisoras;
        }
        else {
            System.out.println("returnednull");
            return new ArrayList<>();
        }
    }


    public List<HtmlElement> extraerTagsEmisora(){
        try {
            return articleTags.getByXPath(AnimeXpaths.relAnimeDetailsRowBroadcast.xpath);
        }
        catch (NullPointerException nullp){
            return null;
        }
    }
    private boolean tieneEmisoras(List<HtmlElement> emisoras){
        return emisoras!=null? !emisoras.isEmpty(): false;
    }


    public boolean contieneObrasRelacionadas(HtmlElement articulo){
        try{
            return !articulo.getByXPath(AnimeXpaths.relAnimeDetailsRelatedMediaTable.xpath).isEmpty();
        }
        catch (NullPointerException nullp){
            return false;
        }
    }


    void iniciarExtraerMusica(HtmlElement article){
        extraerMusica(article);
    }


    void addMusicDataToAnime(AnimeMedia detalle){
        extraerMusica();
        detalle.setOpenings(openingRows);
        detalle.setEndings(endingRows);
    }

    private void extraerMusica(){

        openingRows = new ArrayList<>();
        endingRows = new ArrayList<>();
        extractOpenings(articleTags);
        extractEndings(articleTags);

    }

    private void extraerMusica(HtmlElement article){
            openingRows = new ArrayList<>();
            endingRows = new ArrayList<>();
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
        ArrayList<HtmlElement> tempOpeningRows =new ArrayList<>(tablaOpenings.getByXPath(AnimeXpaths.relAnimeDetailsOpeningsRows.xpath));
        tempOpeningRows.stream()
                .forEach(opRow-> openingRows.add(opRow.getVisibleText()));
    }


    public void extraerTableEndings(HtmlElement article){
        HtmlElement endingsTable=article.getFirstByXPath(AnimeXpaths.relAnimeDetailsEndingsTable.xpath);
        extraerFilasEndings(endingsTable);

    }

    public void extraerFilasEndings(HtmlElement tablaEndings){
        //openingRows.addAll(tablaEndings.getByXPath(AnimeXpaths.relAnimeDetailsOpeningsRows.xpath));
        ArrayList<HtmlElement> tempEndingRows =new ArrayList<>(tablaEndings.getByXPath(AnimeXpaths.relAnimeDetailsEndingsRows.xpath));
        tempEndingRows.stream()
                .forEach(edRow -> endingRows.add(edRow.getVisibleText()));
    }




    public void obtenerDetallesDeEmision(AnimeMedia objetivo){
        usableInformationElements.stream()
                .forEach(infoRow->{
                    HtmlElement tempElem=infoRow;
                    List<HtmlElement>tempDeleteList=infoRow.getByXPath("span[@style=\"display: none\"]");
                    tempDeleteList.stream().forEach(deleteElement->
                    {
                        tempElem.removeChild(deleteElement);
                    });
            String[] infoPair=infoRow.asNormalizedText().split(":",0);
            agregarAHashMapAnime(objetivo,infoPair);

        });

    }
    public void agregarAHashMapAnime(AnimeMedia anime, String[] detallesSeparados){
        try {
            anime.agregarInfoEmision(detallesSeparados[0],detallesSeparados[1]);
        }
        catch (NullPointerException exception){
            System.err.println("No existe un par en "+Arrays.toString(detallesSeparados));
        }
    }

    public Map<String,String> safeExtraerInfoStaff(HtmlElement articleBody){

            return extraerInfoStaff(articleBody);
    }

    private void agregarInfoStaffAdetalles(AnimeMedia detalle){
        detalle.setInfoStaff(extraerInfoStaff());
    }

    private Map<String,String> extraerInfoStaff()  {
        List<HtmlElement> tablaStaff = articleTags.getByXPath(AnimeXpaths.relAnimeDetailsStaffTable.xpath);
        List<HtmlElement> tablasSeparadas = tablaStaff.get(1).getByXPath(AnimeXpaths.relAnimeDetailsStaffIndividualTable.xpath);
        List<String> stringStaff = pasarHtmlElementStaffAString(tablasSeparadas);
        return pasarInformacionStaffAMap(stringStaff);
    }

    private Map<String,String> extraerInfoStaff(HtmlElement articleBody)  {
            List<HtmlElement> tablaStaff = articleBody.getByXPath(AnimeXpaths.relAnimeDetailsStaffTable.xpath);
            List<HtmlElement> tablasSeparadas = tablaStaff.get(1).getByXPath(AnimeXpaths.relAnimeDetailsStaffIndividualTable.xpath);
            List<String> stringStaff = pasarHtmlElementStaffAString(tablasSeparadas);
            return pasarInformacionStaffAMap(stringStaff);
    }

    private List<String> pasarHtmlElementStaffAString(List<HtmlElement> tablasSeparadasStaff){
        List<String> tempStringStaff=new ArrayList<>();
        tablasSeparadasStaff.stream()
                .forEach(tabla->tempStringStaff.add(tabla.asNormalizedText()));
        return tempStringStaff;
    }

    private Map<String,String> pasarInformacionStaffAMap(List<String> staffAsString){
        Map<String,String> tempStaffCargo=new HashMap<>();
        staffAsString.stream()
                .forEach(stringDeStaff->
                {
                    String[] nombreCargo=stringDeStaff.split("\n",2);
                    tempStaffCargo.put(nombreCargo[0],nombreCargo[1]);
                });
        return tempStaffCargo;
    }


    public List<AnimePreview> pasarTodasFilasBusquedaApreview(){
            List<AnimePreview> searchPreviewList = new ArrayList<>();
            searchRowsOfMedia.stream()
                    .forEach(searchRow -> searchPreviewList.add(pasarFilaSearchAPreview(searchRow)));
            System.out.println(searchPreviewList);
            return searchPreviewList;

    }

    public AnimePreview pasarFilaSearchAPreview(HtmlElement filaBusqueda){
        String url=obtenerLinkBusqueda(filaBusqueda);
        int id=obtenerID(url);
        String tipoEmision=obtenerTipoEmisionBusqueda(filaBusqueda);
        String nombre=obtenerNombreBusqueda(filaBusqueda);
        double puntuacion=obtenerPuntajeBusqueda(filaBusqueda);
        return new AnimePreview(id,nombre,tipoEmision,puntuacion,url);

    }

    public String obtenerNombreBusqueda(HtmlElement filaBusqueda){
        String tempNombre=((HtmlElement) filaBusqueda.getFirstByXPath(AnimeXpaths.relAnimeSearchTitle.xpath)).asNormalizedText();
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


    private AnimeMedia safeCrearAnimeDetalles(AnimePreview preview) throws ExcepcionPaginaNoPreparada{
        try {
            return crearAnimeDetalles(preview);
        }
        catch (NullPointerException nullp){
            throw new ExcepcionPaginaNoPreparada();
        }
    }

    AnimeMedia crearAnimeDetalles(AnimePreview preview){
        AnimeMedia detalleAnimeSimple = new AnimeMedia(preview);
        if (detalleAnimeSimple.getPosicionRanking() != 0) {
            return detalleAnimeSimple;
        } else {
            return new AnimeMedia(preview, rankPosFromDetails);
        }
    }
    public List<AnimePreview> getAnimeTopPreview() {
        return animeTopPreview;
    }

}

