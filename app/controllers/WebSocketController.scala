package controllers

import javax.inject._
import actors.{ChatRoom, UserActor}
import akka.actor._
import akka.stream.{Materializer, FlowShape}
import akka.stream.scaladsl.{Merge, Broadcast, GraphDSL, Flow}
import play.api.libs.streams.ActorFlow
import play.api.mvc._


class WebSocketController @Inject()(system: ActorSystem, mat: Materializer) extends Controller {

  implicit val implicitMaterializer: Materializer = mat
  implicit val implicitActorSystem: ActorSystem = system

  val chatRoom = system.actorOf(Props[ChatRoom], "chatRoom")


  def webSocketActor(roomId: Int, username: String) = WebSocket.accept[String, String] { implicit request =>
    ActorFlow.actorRef(UserActor.props(roomId, username))
  }


  def webSocketFlow(roomId: Int, username: String) = WebSocket.accept[String, String]{ implicit request =>
    flow
  }

  val flow = Flow.fromGraph(GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._
    val b = builder.add(Broadcast[String](1))
    val m = builder.add(Merge[String](1))
    val f = Flow[String].map(_ + " from Play!")
    b ~> f ~> m
    FlowShape(b.in, m.out)
  })
}
