package controllers

import com.google.inject.Inject
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config
import uk.gov.dvla.vehicles.presentation.common.model.VehicleDetailsModel

final class DeleteMe @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                       config: Config) extends Controller {

  def present = Action { implicit request =>
    val data = VehicleDetailsModel(registrationNumber = "BD54 XKF", vehicleMake = "Peugeot", vehicleModel = "307")
    Ok(views.html.acquire.keeper_still_on_record(data))
  }

}
