import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
class ChabotPage extends StatefulWidget {
  ChabotPage({super.key});

  @override
  State<ChabotPage> createState() => _ChabotPageState();
}

class _ChabotPageState extends State<ChabotPage> {
  var messages =[

  ];

  TextEditingController userController = TextEditingController();
  ScrollController scrollController = ScrollController();

  void sendChatMessage() {
    String question = userController.text;
    if (question.isEmpty) return;
    
    Uri uri=Uri.parse("https://api.groq.com/openai/v1/chat/completions");
    var headers = {
      "Content-Type":"application/json",
      "Authorization":"Bearer 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"
    };

    setState(() {
      messages.add( {"role": "user", "content": question});
    });
    var body = {
      "model": "llama-3.3-70b-versatile", "messages": messages
    };
    http.post(uri,headers: headers, body: json.encode(body))
     .then((resp) {
       var aiResponse = json.decode(resp.body);
       String answer = aiResponse['choices'][0]['message']['content'];
       setState(() {
         messages.add({"role":"assistant","content":answer});
         scrollController.jumpTo(
             scrollController.position.maxScrollExtent + 800);
       });
       userController.text="";
     }).catchError((err){
       print("**********");
       print(err);
       print("**********");
    });
  }

  @override
  Widget build(BuildContext context) {
    print("Build .......");
    return Scaffold(
      appBar: AppBar(
        title: Text("My Chatbot",
        style: TextStyle(color: Theme.of(context).indicatorColor)
        ),
        backgroundColor: Theme.of(context).primaryColor,
        actions: [
          IconButton(onPressed: (){
            Navigator.of(context).pop();
            Navigator.pushNamed(context, "/");
          }, icon: Icon(Icons.logout,
            color: Theme.of(context).indicatorColor ,))
        ],
      ),
      body: Column(
        children: [
          Expanded(
            child: ListView.builder(
                controller: scrollController,
                itemCount: messages.length,
                itemBuilder: (context, index){
                  return Column(
                    children: [
                      Row(
                        children: [
                          messages[index]['role']=='user'
                              ? SizedBox(width: 80,)
                              : SizedBox(width: 0,),
                          Expanded(
                            child: Card.outlined(
                              margin: EdgeInsets.all(6),
                              color: messages[index]['role']=='user'
                                  ?Colors.blue[50]
                                  : Colors.blue[100]
                              ,
                              child: ListTile(
                                title: Text("${messages[index]['content']}"),
                              ),
                            ),
                          ),
                          messages[index]['role']=='assistant'
                              ? SizedBox(width: 80,)
                              : SizedBox(width: 0,),
                        ],
                      ),
                      Divider()
                    ],
                  );
                },
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(8.0),
            child: Row(
              children: [
                Expanded(
                  child: TextFormField(
                    controller: userController,
                    onFieldSubmitted: (value) => sendChatMessage(),
                    decoration: InputDecoration(
                        hintText: "Saisissez un prompt",
                        //icon: Icon(Icons.lock),
                        //prefixIcon: Icon(Icons.lock),
                        border: OutlineInputBorder(
                            borderRadius: BorderRadius.circular(10),
                            borderSide: BorderSide(
                                width: 1,
                                color: Theme.of(context).primaryColor
                            )
                        )
                    ),
                  ),
                ),
                IconButton(
                  onPressed: sendChatMessage,
                  icon: Icon(Icons.send)
                )
              ],
            ),
          ),
        ],
      ),

    );
  }
}
