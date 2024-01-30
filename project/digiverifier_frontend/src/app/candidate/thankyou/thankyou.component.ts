import { Component, OnInit } from '@angular/core';
import { Router ,ActivatedRoute} from '@angular/router';
import { CandidateService } from 'src/app/services/candidate.service';
import { CustomerService } from '../../services/customer.service';
import { Location } from '@angular/common';
import {AuthenticationService} from "../../services/authentication.service";

@Component({
  selector: 'app-thankyou',
  templateUrl: './thankyou.component.html',
  styleUrls: ['./thankyou.component.scss']
})
export class ThankyouComponent implements OnInit {
  candidateCode: any;
  showvalidation:any=false;
  orgid:any;
  result:any;
  CandidateFormData: any=[];
  constructor(private candidateService: CandidateService,private authService:AuthenticationService,
              private router:ActivatedRoute,private customers:CustomerService, private location: Location) {
    this.orgid= this.authService.getOrgID();
    // this.customers.getShowvalidation(this.orgid).subscribe((data:any)=>{
    //   this.showvalidation=data.outcome;
    //   console.log(this.showvalidation,"result")

    console.log(this.showvalidation,"result")
    if(this.showvalidation == false){
      console.log("inisde ");
      this.candidateCode = this.router.snapshot.paramMap.get('candidateCode');
      console.log(this.candidateCode);
      console.log("before getCandidateFormData THANKYOU FROM>>>>>::{}",new Date().toLocaleTimeString());
      this.candidateService.getCandidateFormData(this.candidateCode).subscribe((data: any)=>{
        this.CandidateFormData=data.data;
        console.log("after getCandidateFormData THANKYOU FROM>>>>>::{}",new Date().toLocaleTimeString());
        console.log(this.CandidateFormData);

        // if(data.outcome === true){
          // console.log("before qcPendingstatus THANKYOU FROM>>>>>::{}",new Date().toLocaleTimeString());
          // this.candidateService.qcPendingstatus(this.candidateCode).subscribe((data:any)=>{
          //  this.result=data.outcome;
          //  console.log("after qcPendingstatus THANKYOU FROM>>>>>::{}",new Date().toLocaleTimeString());
          //  console.log(this.result,"----------------------------------------result")
          // })
        // }
      });
      console.log("before qcPendingstatus THANKYOU FROM>>>>>::{}",new Date().toLocaleTimeString());
      this.candidateService.qcPendingstatus(this.candidateCode).subscribe((data:any)=>{
        this.result=data.outcome;
        console.log("after qcPendingstatus THANKYOU FROM>>>>>::{}",new Date().toLocaleTimeString());
        console.log(this.result,"----------------------------------------result")
       })


    }
  // })
   }
   checkHistoryLength(): boolean {
    return this.location.getState() === null;
  }
  ngOnInit(): void {
    window.addEventListener('popstate', () => {
      if (!this.checkHistoryLength()) {
        this.location.forward();
      }
    });
  }
}
