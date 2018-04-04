package com.kgribov.telegram

import com.kgribov.telegram.model.Message
import com.kgribov.telegram.source.MetaInfoSource


package object security {

  trait ChatPermission {
    def isAllowed(message: Message, metaInfoSource: MetaInfoSource): Boolean

    def permissionsMessage(notAllowedCommand: String, userName: String): String = {
      s"Sorry $userName, but command $notAllowedCommand is not allowed for you"
    }
  }

  def allowEverything(onlyForUsers: Seq[Int] = Seq(),
                      groupUserType: UserType = ANYONE,
                      onlyForGroups: Seq[Int] = Seq()): ChatPermission = {
    new ChatPermissions(Seq(allowPrivateChats(onlyForUsers), allowGroups(groupUserType, onlyForGroups)))
  }

  def allowGroups(groupUserType: UserType = ANYONE, onlyForGroups: Seq[Int] = Seq()): ChatPermission = {
    new GroupsAllowed(groupUserType, onlyForGroups)
  }

  def allowPrivateChats(onlyForUsers: Seq[Int] = Seq()): ChatPermission = {
    new PrivateChatAllowed(onlyForUsers)
  }

  private class ChatPermissions(permissions: Seq[ChatPermission] =
                        Seq(new GroupsAllowed(), new PrivateChatAllowed())) extends ChatPermission {

    override def isAllowed(message: Message, metaInfoSource: MetaInfoSource): Boolean = {
      permissions.exists(_.isAllowed(message, metaInfoSource))
    }
  }

  private class GroupsAllowed(userType: UserType = ANYONE, onlyForGroups: Seq[Int] = Seq()) extends ChatPermission {
    override def isAllowed(message: Message, metaInfoSource: MetaInfoSource): Boolean = {
      val chatId = message.chat.id
      if (!isGroupChat(message) && isOnlyForGroups(chatId, onlyForGroups)) {
        false
      } else {
        if (userType == ADMIN_ONLY) {
          val admins = metaInfoSource.getChatAdministrators(chatId)
          admins.map(_.id).contains(message.from.id) && isOnlyForGroups(chatId, onlyForGroups)
        } else {
          isOnlyForGroups(chatId, onlyForGroups)
        }
      }
    }

    private def isOnlyForGroups(chatId: Int, onlyForGroups: Seq[Int]): Boolean = {
      onlyForGroups.contains(chatId) || onlyForGroups.isEmpty
    }

    private def isGroupChat(message: Message): Boolean = {
      message.chat.chatType == "group" || message.chat.chatType == "supergroup"
    }
  }

  private class PrivateChatAllowed(onlyForUsers: Seq[Int] = Seq()) extends ChatPermission {
    override def isAllowed(message: Message, metaInfoSource: MetaInfoSource): Boolean = {
      if (isPrivateChat(message) && onlyForUsers.isEmpty) {
        true
      } else {
        isPrivateChat(message) && onlyForUsers.contains(message.from.id)
      }
    }

    private def isPrivateChat(message: Message): Boolean = {
      message.chat.chatType == "private"
    }
  }

  private type UserType = String
  val ANYONE: UserType = "ANYONE"
  val ADMIN_ONLY: UserType = "ADMIN_ONLY"
}
