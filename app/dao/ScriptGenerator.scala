package dao

import javax.inject.Inject

class ScriptGenerator @Inject() (
    gameDao: GameDAO,
    gameEggMappingDAO: GameEggMappingDAO,
    userDao: UserDAO
) {

  def generateScripts = {
    println("Starting to generate scripts.....")
    val allscripts = userDao.scripts ++ gameEggMappingDAO.scripts ++ gameDao.scripts
    println(allscripts.mkString("; \n"))
  }

}
