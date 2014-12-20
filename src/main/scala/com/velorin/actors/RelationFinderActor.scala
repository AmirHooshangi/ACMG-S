package com.velorin.actors

import akka.actor.Actor
import akka.actor.ActorRef
import java.net.URL
import com.velorin.model.{NodeList, Node}
import com.velorin.wordnet.WsFactory

class RelationFinderActor(actorRef: ActorRef) extends Actor{

 def receive =  {
    case URLEvent(url: URL, text: String, concepts: List[String]) => {

      val listOfLists = for { i <- 0 to concepts.size - 1

                              relatedList = relatedConcepts(concepts(i), concepts.slice( i+1, concepts.size))

      } yield  {relatedList match  { case Some(s) => relatedList.toList ; case None => List() }}

      println("it comes here")
      listOfLists.filter(x => !x.isEmpty).foreach(println)
      actorRef ! SaveToDBEvent(url, listOfLists.filter(x => !x.isEmpty).toList)
    }
  }

  def relatedConcepts(arg1: String, arg2: List[String]) : Option[NodeList] = {
    
    val list = for { i <- arg2; result = areRelated(arg1,i); weight = result._2  ; if result._1 } yield Node(i,i, weight)
    //TODO: null is avoided in Scala please consider it :)
    if(list.isEmpty) None else Some(NodeList(Node(arg1,arg1,"0"), list))
  }

  def areRelated(concept1: String, concept2: String): (Boolean, String) = {

    val function =  WsFactory.calculationFunction("WuPalmer")
    println("similiarity bet " + concept1 + " and " + concept2 + " = " + function(concept1,concept2))
    val weight = function(concept1,concept2)
    //TODO: 0.4 is a constant, it should be customizable
    if( weight > 0.4 ) (true, weight.toString) else (false , weight.toString)
  }
}
