/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;

import javax.sql.DataSource;
import javax.validation.Valid;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.apache.tomcat.util.http.parser.MediaType;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@SpringBootApplication
public class Main {

  @Value("${spring.datasource.url}")
  private String dbUrl;

  @Autowired
  private DataSource dataSource;

  public static void main(String[] args) throws Exception {
    SpringApplication.run(Main.class, args);
  }

  // @RequestMapping("/")
  // String index() {
  //   return "db";
  // }

  @RequestMapping("/")
  String db(Map<String, Object> model) {
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      
      //  stmt.executeUpdate("CREATE TABLE db_gestores.tb_gestores " +
      //  " ( id_gestor integer NOT NULL GENERATED ALWAYS AS IDENTITY " +
      //  " ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 922337203 CACHE 1 )," +
      //  "  ds_nome text[] COLLATE pg_catalog.\"default\" NOT NULL, " +
      //  "  nr_matricula integer NOT NULL, " + "  dt_nascimento date NOT NULL, " +
      //  "  ds_setor \"char\" NOT NULL, " +
      //  "  CONSTRAINT tb_gestores_pkey PRIMARY KEY (id_gestor)  )");
      //  stmt.executeUpdate("INSERT INTO db_gestores.tb_gestores( " +
      //  "  id_gestor, ds_nome, nr_matricula, dt_nascimento, ds_setor) " +
      //  "  VALUES (?, ?, ?, ?, ?);");
      
      ResultSet rs = stmt.executeQuery("SELECT * FROM tb_gestores");

      ArrayList<JSONObject> output = new ArrayList<JSONObject>();
      while (rs.next()) {
        JSONObject json = new JSONObject();
        json.put("id_gestor", rs.getString("id_gestor"));
        json.put("nome", rs.getString("ds_nome"));
        json.put("matricula", rs.getString("nr_matricula"));
        json.put("setor", rs.getString("ds_setor"));
        json.put("nascimento", rs.getString("dt_nascimento"));
        output.add(json);
      }
      model.put("mensagem", "");
      model.put("records", output);
      return "db";
    } catch (Exception e) {
      model.put("message", e.getMessage());
      return "error";
    }
  }

  @RequestMapping("/editar/{id_gestor}")
  String FormEditar(@PathVariable("id_gestor") int id_gestor, Map<String, Object> model) {
    try (Connection connection = dataSource.getConnection()) {      
        Statement stmt = connection.createStatement();
        
        ResultSet rs = stmt.executeQuery("SELECT * FROM tb_gestores WHERE id_gestor = " + id_gestor );

        ArrayList<JSONObject> output = new ArrayList<JSONObject>();
        while (rs.next()) {
          JSONObject json = new JSONObject();
          json.put("id_gestor", rs.getString("id_gestor"));
          json.put("nome", rs.getString("ds_nome"));
          json.put("matricula", rs.getString("nr_matricula"));
          json.put("setor", rs.getString("ds_setor"));
          json.put("nascimento", rs.getString("dt_nascimento"));
          output.add(json);
        }

        model.put("records", output);
        return "editar";
      
    } catch (Exception e) {
      model.put("message", e.getMessage());
      return "error";
    }
  }
  
  @RequestMapping("/novo")
  String novo() {

    return "novo";
  }

  @PostMapping("/editar/processar_editar")
  String editar(@RequestParam Map<String, String> body,  Map<String, Object> model) {
    String mensagem;
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      String sql = "UPDATE tb_gestores SET ds_nome = '" + body.get("txtNome") + "', nr_matricula = " + body.get("nrMatricula") + ", " +
      " dt_nascimento = '" + body.get("dtNascimento") + "', ds_setor = '" + body.get("lstSetor") + "' " +
      " WHERE id_gestor = " + body.get ("id_gestor");
      if (stmt.executeUpdate(sql) > 1) {
        mensagem = "Gestor atualizado com sucesso";
        novo();
      } else {
        return "error";
      }
      
    } catch (Exception e) {
      return "error";
    }
    model.put("mensagem", mensagem);
    return "/";
    
  }

  @PostMapping( "novo" )
  String novo( @RequestParam Map<String, String> body,  Map<String, Object> model) {
    String mensagem;
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      String sql = "INSERT INTO tb_gestores( ds_nome, nr_matricula, dt_nascimento, ds_setor) "
      + "  VALUES ( '{" + body.get("txtNome") + "}', '" + body.get("nrMatricula") + "', '"
      + body.get("dtNascimento") + "', '" + body.get("lstSetor") + "')";
      //return sql;
      stmt.executeQuery(sql);
      mensagem = "Gestor cadastrado com sucesso";

    } catch (Exception e) {
      return "error";
    }
    model.put("mensagem", mensagem);
    return "/";
  }

  @RequestMapping("excluir/{id_gestor}")
  String excluir (@PathVariable("id_gestor") int id_gestor,  Map<String, Object> model) {
    String mensagem;
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      String sql = "DELETE from tb_gestores WHERE id_gestor =  " + id_gestor;
      //return sql;
      stmt.executeUpdate(sql);
      mensagem = "Gestor excluido com sucesso";
    }
    catch (Exception e) {
      mensagem = "Ocorreu um erro ao excluir gestor";
    }
    model.put("mensagem", mensagem);
    return "db";
  }
  /*
   * @PutMapping("/editar/{id_gestor}") Gestores editar(@Valid @RequestBody
   * Gestores gestor){
   * 
   * try (Connection connection = dataSource.getConnection()) { Statement stmt =
   * connection.createStatement();
   * 
   * stmt.executeQuery("UPDATE db_gestores.tb_gestores SET  " + "  ds_nome ='"+
   * gestor.getDs_nome() +"', nr_matricula = '"+ gestor.getNr_matricula()
   * +"', dt_nascimento = '"+ gestor.getDt_nascimento() +"', ds_setor = '"+
   * gestor.getDs_setor() +"') " + "  VALUES ();");
   * 
   * 
   * } */
    
    //  @DeleteMapping("excluir/{id_gestor}") public ResponseEntity<?>
    //  deleteGestor(@PathVariable int id_gestor) { return
    //  service.deleteById_gestor(id_gestor); }


  @Bean
  public DataSource dataSource() throws SQLException {
    if (dbUrl == null || dbUrl.isEmpty()) {
      return new HikariDataSource();
    } else {
      HikariConfig config = new HikariConfig();
      config.setJdbcUrl(dbUrl);
      return new HikariDataSource(config);
    }
  }

}
