package com.webservice.peglefiles;

import com.models.StationListObject;
import com.models.StationsList;
import com.webservice.peglefiles.models.answear.StationsListResponse;
import com.webservice.peglefiles.models.request.StationsListRequest;
import com.webservice.peglefiles.webservices.ListaStacjiWebService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
@RequestMapping("/stacje")
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS, value = "request")
public class StationsListController extends BaseController {
    @Autowired
    ListaStacjiWebService listaStacjiWS;

    private static final Logger logger = Logger.getLogger(StationsListController.class.getName());

    @ResponseBody
    @RequestMapping(value = "/get", method = RequestMethod.GET, produces = "application/json")
    public List<StationListObject> getStations(/*@RequestBody StationsListRequest request*/) throws Exception {
        logger.log(Level.SEVERE, "Uruchomienie kontrolera");
        List<StationListObject> list = listaStacjiWS.getListaStacji().toBlocking().first();
        logger.log(Level.SEVERE, list.get(0).getName());
        return list;
    }
}
