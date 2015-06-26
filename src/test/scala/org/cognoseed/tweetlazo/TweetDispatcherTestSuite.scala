package org.cognoseed.tweetlazo

import akka.actor.SupervisorStrategy.Stop
import akka.actor._
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import org.cognoseed.tweetlazo.TweetDispatcher.WatchHashtag
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike}
import twitter4j.{HashtagEntity, Status}

class TweetDispatcherTestSuite extends TestKit(ActorSystem("TestSystem")) with FunSuiteLike with BeforeAndAfterAll
  with ImplicitSender with MockFactory {

  override def afterAll(): Unit = {
    shutdown(system)
  }

  trait CommonTweetDispatcherTest {
    val maker = mockFunction[ActorRefFactory, String, ActorRef]
    maker.expects(*, "test").returning(testActor)
    val dispatcher = TestActorRef[TweetDispatcher](TweetDispatcher.props(maker))
    dispatcher ! WatchHashtag("test")
  }

  trait ExceptionThrowingTest {
    val maker = mockFunction[ActorRefFactory, String, ActorRef]
    maker.expects(*, "test").returning(testActor)
    val dispatcher = system.actorOf(Props(new Actor {
      val realDispatcher = context.actorOf(TweetDispatcher.props(maker))
      override val supervisorStrategy = OneForOneStrategy() {
        case f =>
          testActor ! "fail"
          Stop
      }
      def receive = {
        case msg => realDispatcher forward msg
      }
    }))
    dispatcher ! WatchHashtag("test")
  }

  test("TweetDispatcher spawns a child when sent a WatchHashtag message") {
    new CommonTweetDispatcherTest {
      assert(dispatcher.underlyingActor.children("test") == testActor)
    }
  }

  test("TweetDispatcher relays tweets to a child when it finds watched hashtags") {
    new CommonTweetDispatcherTest {
      val fakeTweet = mock[Status]
      val fakeHashtag = mock[HashtagEntity]
      (fakeHashtag.getText _).expects().returning("test")
      (fakeTweet.getHashtagEntities _).expects().returning(Array(fakeHashtag))

      dispatcher ! fakeTweet
      expectMsg(fakeTweet)
    }
  }

  test("TweetDispatcher should ignore WatchHashtag messages for hashtags that are already being watched") {
    new ExceptionThrowingTest {
      dispatcher ! WatchHashtag("test")
      expectNoMsg()
    }
  }

  test("TweetDispatcher should ignore UnwatchHashtag messages for hashtags that aren't being watched") {
    pending
  }

}
